-- Backend verification for TopUp: wallet provisioning, RLS isolation, client write denial and
-- process_recharge business rules.
--
-- The whole script is one DO block that always finishes with RAISE EXCEPTION, so every change it
-- makes (test users, wallets, transactions) is rolled back in any SQL client. The exception
-- message is a JSON array of {check, passed, detail}; a run is green when every item has
-- "passed": true. Run it as a privileged role (postgres), e.g. Supabase SQL editor or MCP.

do $$
declare
  v_user_a uuid := gen_random_uuid();
  v_user_b uuid := gen_random_uuid();
  v_product_1000 uuid;
  v_product_30000 uuid;
  v_atom_product uuid;
  v_key uuid := gen_random_uuid();
  v_result jsonb;
  v_count integer;
  v_balance bigint;
  v_results jsonb := '[]'::jsonb;
  v_sql text;
  v_denied boolean;
  v_denial_checks text[] := array[
    'select count(*) from public.operator_prefixes',
    'update public.wallets set balance_mmk = 999999999',
    'insert into public.wallets (user_id) values (gen_random_uuid())',
    'delete from public.wallets',
    'insert into public.transactions (user_id, idempotency_key, request_fingerprint, phone_number, operator_code, operator_name, operator_logo_bucket, operator_logo_path, product_type, product_name, amount_mmk, balance_after_mmk) values (auth.uid(), gen_random_uuid(), ''x'', ''09420000001'', ''MPT'', ''MPT'', ''telecom'', ''mpt.jpeg'', ''CUSTOM_AMOUNT'', ''x'', 1, 1)',
    'update public.transactions set amount_mmk = 1',
    'update public.transactions set user_id = gen_random_uuid()',
    'delete from public.transactions',
    'update public.catalog_products set price_mmk = 1',
    'insert into public.operators (code, name, display_name, logo_path, custom_amount_min_mmk, custom_amount_max_mmk, custom_amount_step_mmk) values (''FAKE'', ''x'', ''x'', ''x'', 1, 1, 1)',
    'select public.process_recharge(auth.uid(), gen_random_uuid(), ''09420000001'', ''MPT'', null, 1000)'
  ];
begin
  -- Provisioning ----------------------------------------------------------------
  insert into auth.users (id, instance_id, aud, role, email, encrypted_password, raw_app_meta_data, raw_user_meta_data, created_at, updated_at)
  values
    (v_user_a, '00000000-0000-0000-0000-000000000000', 'authenticated', 'authenticated', 'rls-a@topup.test', '', '{}', '{}', now(), now()),
    (v_user_b, '00000000-0000-0000-0000-000000000000', 'authenticated', 'authenticated', 'rls-b@topup.test', '', '{}', '{}', now(), now());

  select count(*) into v_count from public.wallets
  where user_id in (v_user_a, v_user_b) and balance_mmk = 500000 and currency = 'MMK';
  v_results := v_results || jsonb_build_object('check', 'wallet provisioned with 500,000 MMK for new users', 'passed', v_count = 2, 'detail', v_count);

  select count(*) into v_count from public.profiles where id in (v_user_a, v_user_b);
  v_results := v_results || jsonb_build_object('check', 'profile provisioned for new users', 'passed', v_count = 2, 'detail', v_count);

  insert into public.wallets (user_id) values (v_user_a) on conflict (user_id) do nothing;
  select count(*) into v_count from public.wallets where user_id = v_user_a;
  v_results := v_results || jsonb_build_object('check', 'wallet provisioning is idempotent', 'passed', v_count = 1, 'detail', v_count);

  select id into v_product_1000 from public.catalog_products where sku = 'MPT-AIRTIME-1000';
  select id into v_product_30000 from public.catalog_products where sku = 'MPT-AIRTIME-30000';
  select id into v_atom_product from public.catalog_products where sku = 'ATOM-AIRTIME-1000';

  -- Recharge rules (service_role path) ------------------------------------------
  v_result := public.process_recharge(v_user_a, v_key, '09420000001', 'MPT', v_product_1000, null);
  select balance_mmk into v_balance from public.wallets where user_id = v_user_a;
  v_results := v_results || jsonb_build_object(
    'check', 'successful recharge deducts authoritative price and records balance after',
    'passed', coalesce((v_result ->> 'ok')::boolean, false) and v_balance = 499000
      and (v_result -> 'transaction' ->> 'balance_after_mmk')::bigint = 499000
      and (v_result -> 'transaction' ->> 'amount_mmk')::int = 1000
      and v_result -> 'transaction' ->> 'status' = 'SUCCESS',
    'detail', v_result);

  v_results := v_results || jsonb_build_object(
    'check', 'recharge response omits internal columns',
    'passed', not ((v_result -> 'transaction') ? 'user_id') and not ((v_result -> 'transaction') ? 'request_fingerprint'),
    'detail', null);

  v_result := public.process_recharge(v_user_a, v_key, '09420000001', 'MPT', v_product_1000, null);
  select balance_mmk into v_balance from public.wallets where user_id = v_user_a;
  select count(*) into v_count from public.transactions where user_id = v_user_a;
  v_results := v_results || jsonb_build_object(
    'check', 'duplicate idempotency key replays without charging again',
    'passed', coalesce((v_result ->> 'replayed')::boolean, false) and v_balance = 499000 and v_count = 1,
    'detail', jsonb_build_object('balance', v_balance, 'transactions', v_count));

  v_result := public.process_recharge(v_user_a, v_key, '09420000002', 'MPT', v_product_1000, null);
  v_results := v_results || jsonb_build_object('check', 'reused idempotency key with different payload is rejected',
    'passed', v_result ->> 'code' = 'IDEMPOTENCY_KEY_REUSED', 'detail', v_result);

  v_result := public.process_recharge(v_user_a, gen_random_uuid(), '09790000001', 'MPT', v_product_1000, null);
  v_results := v_results || jsonb_build_object('check', 'phone prefix must match requested operator',
    'passed', v_result ->> 'code' = 'OPERATOR_MISMATCH', 'detail', v_result);

  v_result := public.process_recharge(v_user_a, gen_random_uuid(), '09269000001', 'MPT', v_product_1000, null);
  v_results := v_results || jsonb_build_object('check', 'unsupported prefix 09269 is rejected',
    'passed', v_result ->> 'code' = 'UNSUPPORTED_OPERATOR', 'detail', v_result);

  v_result := public.process_recharge(v_user_a, gen_random_uuid(), '0942000000', 'MPT', v_product_1000, null);
  v_results := v_results || jsonb_build_object('check', 'invalid phone number is rejected',
    'passed', v_result ->> 'code' = 'INVALID_PHONE_NUMBER', 'detail', v_result);

  v_result := public.process_recharge(v_user_a, gen_random_uuid(), '09420000001', 'MPT', v_atom_product, null);
  v_results := v_results || jsonb_build_object('check', 'product must belong to requested operator',
    'passed', v_result ->> 'code' = 'PRODUCT_OPERATOR_MISMATCH', 'detail', v_result);

  v_result := public.process_recharge(v_user_a, gen_random_uuid(), '09420000001', 'MPT', null, 1050);
  v_results := v_results || jsonb_build_object('check', 'custom amount must respect step',
    'passed', v_result ->> 'code' = 'INVALID_AMOUNT', 'detail', v_result);

  v_result := public.process_recharge(v_user_a, gen_random_uuid(), '09420000001', 'MPT', null, 1500);
  v_results := v_results || jsonb_build_object('check', 'valid custom amount succeeds',
    'passed', coalesce((v_result ->> 'ok')::boolean, false) and v_result -> 'transaction' ->> 'product_type' = 'CUSTOM_AMOUNT',
    'detail', v_result -> 'transaction' -> 'amount_mmk');

  update public.wallets set balance_mmk = 20000 where user_id = v_user_a;
  select count(*) into v_count from public.transactions where user_id = v_user_a;
  v_result := public.process_recharge(v_user_a, gen_random_uuid(), '09420000001', 'MPT', v_product_30000, null);
  select balance_mmk into v_balance from public.wallets where user_id = v_user_a;
  v_results := v_results || jsonb_build_object(
    'check', 'insufficient balance rejects without deduction or history row',
    'passed', v_result ->> 'code' = 'INSUFFICIENT_BALANCE' and v_balance = 20000
      and (select count(*) from public.transactions where user_id = v_user_a) = v_count,
    'detail', v_result);

  -- RLS reads as user A ---------------------------------------------------------
  perform set_config('request.jwt.claims', json_build_object('sub', v_user_a, 'role', 'authenticated')::text, true);

  execute 'set local role authenticated';
  select count(*) into v_count from public.wallets;
  execute 'reset role';
  v_results := v_results || jsonb_build_object('check', 'user A sees only own wallet', 'passed', v_count = 1, 'detail', v_count);

  execute 'set local role authenticated';
  select count(*) into v_count from public.transactions;
  execute 'reset role';
  v_results := v_results || jsonb_build_object('check', 'user A sees own transactions', 'passed', v_count = 2, 'detail', v_count);

  execute 'set local role authenticated';
  select count(*) into v_count from public.list_my_transactions(p_search => '0942', p_limit => 20);
  execute 'reset role';
  v_results := v_results || jsonb_build_object('check', 'list_my_transactions returns own phone matches', 'passed', v_count = 2, 'detail', v_count);

  execute 'set local role authenticated';
  select count(*) into v_count from public.list_my_transactions(p_operator_codes => array['ATOM']);
  execute 'reset role';
  v_results := v_results || jsonb_build_object('check', 'list_my_transactions operator filter', 'passed', v_count = 0, 'detail', v_count);

  execute 'set local role authenticated';
  select count(*) into v_count from public.operators;
  execute 'reset role';
  v_results := v_results || jsonb_build_object('check', 'authenticated users can read catalog operators', 'passed', v_count = 4, 'detail', v_count);

  execute 'set local role authenticated';
  select count(*) into v_count from public.catalog_products;
  execute 'reset role';
  v_results := v_results || jsonb_build_object('check', 'authenticated users can read catalog products', 'passed', v_count = 44, 'detail', v_count);

  -- Client writes and privileged calls are denied --------------------------------
  foreach v_sql in array v_denial_checks loop
    begin
      perform set_config('request.jwt.claims', json_build_object('sub', v_user_a, 'role', 'authenticated')::text, true);
      execute 'set local role authenticated';
      execute v_sql;
      v_denied := false;
      execute 'reset role';
    exception when insufficient_privilege then
      v_denied := true;
      execute 'reset role';
    end;
    v_results := v_results || jsonb_build_object('check', 'authenticated client denied: ' || left(v_sql, 70), 'passed', v_denied, 'detail', null);
  end loop;

  select balance_mmk into v_balance from public.wallets where user_id = v_user_a;
  v_results := v_results || jsonb_build_object('check', 'wallet unchanged after denied client writes', 'passed', v_balance = 20000, 'detail', v_balance);

  -- RLS as user B ---------------------------------------------------------------
  perform set_config('request.jwt.claims', json_build_object('sub', v_user_b, 'role', 'authenticated')::text, true);

  execute 'set local role authenticated';
  select count(*) into v_count from public.transactions;
  execute 'reset role';
  v_results := v_results || jsonb_build_object('check', 'user B cannot see user A transactions', 'passed', v_count = 0, 'detail', v_count);

  execute 'set local role authenticated';
  select count(*) into v_count from public.wallets where user_id = v_user_a;
  execute 'reset role';
  v_results := v_results || jsonb_build_object('check', 'user B cannot see user A wallet', 'passed', v_count = 0, 'detail', v_count);

  execute 'set local role authenticated';
  select count(*) into v_count from public.profiles where id = v_user_a;
  execute 'reset role';
  v_results := v_results || jsonb_build_object('check', 'user B cannot see user A profile', 'passed', v_count = 0, 'detail', v_count);

  execute 'set local role authenticated';
  select count(*) into v_count from public.list_my_transactions();
  execute 'reset role';
  v_results := v_results || jsonb_build_object('check', 'list_my_transactions is scoped to caller', 'passed', v_count = 0, 'detail', v_count);

  -- anon ------------------------------------------------------------------------
  perform set_config('request.jwt.claims', json_build_object('role', 'anon')::text, true);
  foreach v_sql in array array[
    'select count(*) from public.wallets',
    'select count(*) from public.transactions',
    'select count(*) from public.catalog_products',
    'select count(*) from public.list_my_transactions()'
  ] loop
    begin
      execute 'set local role anon';
      execute v_sql;
      v_denied := false;
      execute 'reset role';
    exception when insufficient_privilege then
      v_denied := true;
      execute 'reset role';
    end;
    v_results := v_results || jsonb_build_object('check', 'anon denied: ' || v_sql, 'passed', v_denied, 'detail', null);
  end loop;

  raise exception 'TOPUP_TEST_RESULTS %', jsonb_pretty(jsonb_build_object(
    'all_passed', not exists (select 1 from jsonb_array_elements(v_results) r where not (r ->> 'passed')::boolean),
    'results', v_results
  ));
end;
$$;
