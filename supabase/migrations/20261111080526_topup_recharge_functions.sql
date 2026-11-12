-- Recharge processing and history query functions.

-- ---------------------------------------------------------------------------
-- process_recharge
--
-- The only code path that deducts wallet balance or creates a transaction. Runs in a single
-- transaction (one PostgREST RPC call) and:
--   1. validates the request against server-side data (prefixes, operator, catalog price),
--   2. locks the caller's wallet row (FOR UPDATE) so concurrent recharges are serialized,
--   3. replays an already-processed idempotency key instead of charging again,
--   4. rejects insufficient balance without writing anything,
--   5. deducts the authoritative price and stores a snapshot of the purchased product.
--
-- p_user_id comes from the Edge Function after it verified the caller's JWT; EXECUTE is granted
-- to service_role only, so end users cannot call this function directly.
-- Business failures are returned as {ok: false, code: ...} so nothing is written for them.
-- ---------------------------------------------------------------------------

create or replace function public.process_recharge(
  p_user_id uuid,
  p_idempotency_key uuid,
  p_phone_number text,
  p_operator_code text,
  p_product_id uuid default null,
  p_custom_amount_mmk integer default null
)
returns jsonb
language plpgsql
security invoker
set search_path = ''
set lock_timeout = '5s'
as $$
declare
  v_fingerprint text;
  v_wallet public.wallets%rowtype;
  v_existing public.transactions%rowtype;
  v_operator public.operators%rowtype;
  v_product public.catalog_products%rowtype;
  v_detected_operator text;
  v_amount integer;
  v_transaction public.transactions%rowtype;
begin
  if p_user_id is null or p_idempotency_key is null then
    return jsonb_build_object('ok', false, 'code', 'INVALID_REQUEST');
  end if;

  if (p_product_id is null) = (p_custom_amount_mmk is null) then
    return jsonb_build_object('ok', false, 'code', 'INVALID_REQUEST');
  end if;

  if p_phone_number is null or p_phone_number !~ '^09[0-9]{9}$' then
    return jsonb_build_object('ok', false, 'code', 'INVALID_PHONE_NUMBER');
  end if;

  v_fingerprint := concat_ws(
    '|',
    p_phone_number,
    p_operator_code,
    coalesce(p_product_id::text, 'custom:' || p_custom_amount_mmk::text)
  );

  -- Serialize every balance change for this wallet.
  select * into v_wallet
  from public.wallets
  where user_id = p_user_id
  for update;

  if not found then
    return jsonb_build_object('ok', false, 'code', 'WALLET_NOT_FOUND');
  end if;

  select * into v_existing
  from public.transactions
  where user_id = p_user_id and idempotency_key = p_idempotency_key;

  if found then
    if v_existing.request_fingerprint <> v_fingerprint then
      return jsonb_build_object('ok', false, 'code', 'IDEMPOTENCY_KEY_REUSED');
    end if;

    return jsonb_build_object(
      'ok', true,
      'replayed', true,
      'transaction', to_jsonb(v_existing) - 'user_id' - 'idempotency_key' - 'request_fingerprint',
      'wallet_balance_mmk', v_wallet.balance_mmk
    );
  end if;

  select * into v_operator
  from public.operators
  where code = p_operator_code and is_active;

  if not found then
    return jsonb_build_object('ok', false, 'code', 'UNSUPPORTED_OPERATOR');
  end if;

  select operator_code into v_detected_operator
  from public.operator_prefixes
  where starts_with(p_phone_number, prefix)
  order by length(prefix) desc
  limit 1;

  if v_detected_operator is null then
    return jsonb_build_object('ok', false, 'code', 'UNSUPPORTED_OPERATOR');
  end if;

  if v_detected_operator <> p_operator_code then
    return jsonb_build_object('ok', false, 'code', 'OPERATOR_MISMATCH');
  end if;

  if p_product_id is not null then
    select * into v_product
    from public.catalog_products
    where id = p_product_id;

    if not found or not v_product.is_active then
      return jsonb_build_object('ok', false, 'code', 'PRODUCT_NOT_AVAILABLE');
    end if;

    if v_product.operator_code <> p_operator_code then
      return jsonb_build_object('ok', false, 'code', 'PRODUCT_OPERATOR_MISMATCH');
    end if;

    v_amount := v_product.price_mmk;
  else
    if p_custom_amount_mmk < v_operator.custom_amount_min_mmk
      or p_custom_amount_mmk > v_operator.custom_amount_max_mmk
      or p_custom_amount_mmk % v_operator.custom_amount_step_mmk <> 0 then
      return jsonb_build_object(
        'ok', false,
        'code', 'INVALID_AMOUNT',
        'min_amount_mmk', v_operator.custom_amount_min_mmk,
        'max_amount_mmk', v_operator.custom_amount_max_mmk,
        'step_mmk', v_operator.custom_amount_step_mmk
      );
    end if;

    v_amount := p_custom_amount_mmk;
  end if;

  if v_wallet.balance_mmk < v_amount then
    return jsonb_build_object(
      'ok', false,
      'code', 'INSUFFICIENT_BALANCE',
      'amount_mmk', v_amount,
      'wallet_balance_mmk', v_wallet.balance_mmk
    );
  end if;

  update public.wallets
  set balance_mmk = balance_mmk - v_amount
  where user_id = p_user_id
  returning * into v_wallet;

  insert into public.transactions (
    user_id,
    idempotency_key,
    request_fingerprint,
    phone_number,
    operator_code,
    operator_name,
    operator_logo_bucket,
    operator_logo_path,
    product_id,
    product_sku,
    product_type,
    product_name,
    data_allowance_mb,
    call_minutes,
    services,
    validity_days,
    amount_mmk,
    currency,
    status,
    balance_after_mmk
  ) values (
    p_user_id,
    p_idempotency_key,
    v_fingerprint,
    p_phone_number,
    v_operator.code,
    v_operator.display_name,
    v_operator.logo_bucket,
    v_operator.logo_path,
    v_product.id,
    v_product.sku,
    coalesce(v_product.product_type, 'CUSTOM_AMOUNT'),
    coalesce(v_product.name, 'Custom amount'),
    v_product.data_allowance_mb,
    v_product.call_minutes,
    coalesce(v_product.services, '{}'),
    v_product.validity_days,
    v_amount,
    'MMK',
    'SUCCESS',
    v_wallet.balance_mmk
  )
  returning * into v_transaction;

  return jsonb_build_object(
    'ok', true,
    'replayed', false,
    'transaction', to_jsonb(v_transaction) - 'user_id' - 'idempotency_key' - 'request_fingerprint',
    'wallet_balance_mmk', v_wallet.balance_mmk
  );
end;
$$;

revoke all on function public.process_recharge(uuid, uuid, text, text, uuid, integer)
  from public, anon, authenticated;
grant execute on function public.process_recharge(uuid, uuid, text, text, uuid, integer)
  to service_role;

-- ---------------------------------------------------------------------------
-- list_my_transactions
--
-- Server-side search / filter / sort / pagination over the caller's own transactions.
-- SECURITY INVOKER: RLS on public.transactions applies in addition to the auth.uid() predicate.
-- Ordering is deterministic (created_at, id) so offset pagination never repeats or skips rows
-- for a stable data set.
-- ---------------------------------------------------------------------------

create or replace function public.list_my_transactions(
  p_search text default null,
  p_operator_codes text[] default null,
  p_created_from timestamptz default null,
  p_created_to timestamptz default null,
  p_oldest_first boolean default false,
  p_limit integer default 20,
  p_offset integer default 0
)
returns setof public.transactions
language sql
stable
security invoker
set search_path = ''
as $$
  with params as (
    select
      nullif(
        replace(replace(replace(btrim(coalesce(p_search, '')), '\', '\\'), '%', '\%'), '_', '\_'),
        ''
      ) as search_pattern
  )
  select t.*
  from public.transactions t, params
  where t.user_id = (select auth.uid())
    and (
      params.search_pattern is null
      or t.phone_number ilike '%' || params.search_pattern || '%'
      or t.id::text ilike '%' || params.search_pattern || '%'
    )
    and (
      p_operator_codes is null
      or cardinality(p_operator_codes) = 0
      or t.operator_code = any (p_operator_codes)
    )
    and (p_created_from is null or t.created_at >= p_created_from)
    and (p_created_to is null or t.created_at < p_created_to)
  order by
    case when p_oldest_first then t.created_at end asc,
    case when p_oldest_first then t.id end asc,
    t.created_at desc,
    t.id desc
  limit least(greatest(coalesce(p_limit, 20), 1), 100)
  offset greatest(coalesce(p_offset, 0), 0);
$$;

revoke all on function public.list_my_transactions(text, text[], timestamptz, timestamptz, boolean, integer, integer)
  from public, anon;
grant execute on function public.list_my_transactions(text, text[], timestamptz, timestamptz, boolean, integer, integer)
  to authenticated;
