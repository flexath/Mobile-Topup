-- TopUp core schema: profiles, demo wallets, telecom catalog and successful recharge transactions.
--
-- Access model
--   * Clients (anon / authenticated) can only SELECT; every write is server-controlled.
--   * Users read only their own profile, wallet and transactions (auth.uid() + RLS).
--   * Wallet deductions and transaction inserts happen exclusively in public.process_recharge,
--     which only service_role may execute (called by the process-recharge Edge Function).

create schema if not exists app_private;
revoke all on schema app_private from public;

-- ---------------------------------------------------------------------------
-- Shared trigger helpers
-- ---------------------------------------------------------------------------

create or replace function app_private.set_updated_at()
returns trigger
language plpgsql
set search_path = ''
as $$
begin
  new.updated_at := now();
  return new;
end;
$$;

-- ---------------------------------------------------------------------------
-- Profiles
-- ---------------------------------------------------------------------------

create table if not exists public.profiles (
  id uuid primary key references auth.users (id) on delete cascade,
  email text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

comment on table public.profiles is 'Minimal application profile linked 1:1 to auth.users.';

-- ---------------------------------------------------------------------------
-- Wallets (one simulated wallet per user, whole Myanmar kyat)
-- ---------------------------------------------------------------------------

create table if not exists public.wallets (
  user_id uuid primary key references auth.users (id) on delete cascade,
  balance_mmk bigint not null default 500000,
  currency text not null default 'MMK',
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  constraint wallets_balance_non_negative check (balance_mmk >= 0),
  constraint wallets_currency_mmk check (currency = 'MMK')
);

comment on table public.wallets is 'Simulated demo wallet. Provisioned by trigger on auth.users; only process_recharge may change the balance.';
comment on column public.wallets.balance_mmk is 'Balance in whole MMK. The 500,000 MMK initial demo balance is a backend default and is never set by the client.';

-- ---------------------------------------------------------------------------
-- Telecom operators
-- ---------------------------------------------------------------------------

create table if not exists public.operators (
  code text primary key,
  name text not null,
  display_name text not null,
  logo_bucket text not null default 'telecom',
  logo_path text not null,
  custom_amount_min_mmk integer not null,
  custom_amount_max_mmk integer not null,
  custom_amount_step_mmk integer not null,
  sort_order smallint not null default 0,
  is_active boolean not null default true,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  constraint operators_code_format check (code ~ '^[A-Z]+$'),
  constraint operators_custom_amount_min_positive check (custom_amount_min_mmk > 0),
  constraint operators_custom_amount_range check (custom_amount_max_mmk >= custom_amount_min_mmk),
  constraint operators_custom_amount_step_positive check (custom_amount_step_mmk > 0)
);

comment on table public.operators is 'Supported Myanmar telecom operators. Logo files live in Supabase Storage (logo_bucket/logo_path).';
comment on column public.operators.custom_amount_min_mmk is 'Demo rule: smallest custom top-up amount accepted by process_recharge.';

-- Server-side copy of the PRD common-prefix dataset, used to validate recharge requests.
-- The Android app keeps its own offline copy for detection; this table is never exposed to clients.
create table if not exists public.operator_prefixes (
  prefix text primary key,
  operator_code text not null references public.operators (code) on update cascade,
  created_at timestamptz not null default now(),
  constraint operator_prefixes_format check (prefix ~ '^09[0-9]{2,3}$')
);

create index if not exists operator_prefixes_operator_code_idx
  on public.operator_prefixes (operator_code);

-- ---------------------------------------------------------------------------
-- Catalog (denominations + data packages)
-- ---------------------------------------------------------------------------

create table if not exists public.catalog_products (
  id uuid primary key default gen_random_uuid(),
  sku text not null,
  operator_code text not null references public.operators (code) on update cascade,
  product_type text not null,
  name text not null,
  price_mmk integer not null,
  data_allowance_mb integer,
  call_minutes integer,
  services text[] not null default '{}',
  validity_days integer,
  is_demo boolean not null default true,
  is_active boolean not null default true,
  sort_order integer not null default 0,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  constraint catalog_products_sku_key unique (sku),
  constraint catalog_products_type check (product_type in ('DENOMINATION', 'DATA_PACKAGE')),
  constraint catalog_products_price_positive check (price_mmk > 0),
  constraint catalog_products_data_positive check (data_allowance_mb is null or data_allowance_mb > 0),
  constraint catalog_products_minutes_positive check (call_minutes is null or call_minutes > 0),
  constraint catalog_products_validity_positive check (validity_days is null or validity_days > 0)
);

comment on table public.catalog_products is 'Unified top-up catalog for all operators. Rows with is_demo = true are demonstration data, not real operator offers.';

create index if not exists catalog_products_operator_type_idx
  on public.catalog_products (operator_code, product_type, sort_order);

-- ---------------------------------------------------------------------------
-- Transactions (successful simulated recharges only)
-- ---------------------------------------------------------------------------

create table if not exists public.transactions (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users (id) on delete cascade,
  idempotency_key uuid not null,
  request_fingerprint text not null,
  phone_number text not null,
  operator_code text not null references public.operators (code) on update cascade,
  operator_name text not null,
  operator_logo_bucket text not null,
  operator_logo_path text not null,
  product_id uuid references public.catalog_products (id) on delete set null,
  product_sku text,
  product_type text not null,
  product_name text not null,
  data_allowance_mb integer,
  call_minutes integer,
  services text[] not null default '{}',
  validity_days integer,
  amount_mmk integer not null,
  currency text not null default 'MMK',
  status text not null default 'SUCCESS',
  balance_after_mmk bigint not null,
  created_at timestamptz not null default now(),
  constraint transactions_user_idempotency_key unique (user_id, idempotency_key),
  constraint transactions_phone_format check (phone_number ~ '^09[0-9]{9}$'),
  constraint transactions_product_type check (product_type in ('DENOMINATION', 'DATA_PACKAGE', 'CUSTOM_AMOUNT')),
  constraint transactions_amount_positive check (amount_mmk > 0),
  constraint transactions_currency_mmk check (currency = 'MMK'),
  constraint transactions_status_success check (status = 'SUCCESS'),
  constraint transactions_balance_after_non_negative check (balance_after_mmk >= 0)
);

comment on table public.transactions is 'Successful simulated recharges. Operator and package fields are a snapshot taken at purchase time.';

create index if not exists transactions_user_created_idx
  on public.transactions (user_id, created_at desc, id desc);
create index if not exists transactions_operator_code_idx
  on public.transactions (operator_code);
create index if not exists transactions_product_id_idx
  on public.transactions (product_id);

-- ---------------------------------------------------------------------------
-- updated_at triggers
-- ---------------------------------------------------------------------------

drop trigger if exists set_updated_at on public.profiles;
create trigger set_updated_at before update on public.profiles
  for each row execute function app_private.set_updated_at();

drop trigger if exists set_updated_at on public.wallets;
create trigger set_updated_at before update on public.wallets
  for each row execute function app_private.set_updated_at();

drop trigger if exists set_updated_at on public.operators;
create trigger set_updated_at before update on public.operators
  for each row execute function app_private.set_updated_at();

drop trigger if exists set_updated_at on public.catalog_products;
create trigger set_updated_at before update on public.catalog_products
  for each row execute function app_private.set_updated_at();

-- ---------------------------------------------------------------------------
-- Profile + wallet provisioning (idempotent)
-- ---------------------------------------------------------------------------

create or replace function app_private.handle_new_auth_user()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
begin
  insert into public.profiles (id, email)
  values (new.id, new.email)
  on conflict (id) do nothing;

  insert into public.wallets (user_id)
  values (new.id)
  on conflict (user_id) do nothing;

  return new;
end;
$$;

revoke all on function app_private.handle_new_auth_user() from public, anon, authenticated;
grant usage on schema app_private to supabase_auth_admin;
grant execute on function app_private.handle_new_auth_user() to supabase_auth_admin;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
  after insert on auth.users
  for each row execute function app_private.handle_new_auth_user();

-- Backfill users that existed before this migration.
insert into public.profiles (id, email)
select u.id, u.email from auth.users u
on conflict (id) do nothing;

insert into public.wallets (user_id)
select u.id from auth.users u
on conflict (user_id) do nothing;

-- ---------------------------------------------------------------------------
-- Privileges: clients are read-only
-- ---------------------------------------------------------------------------

revoke all on table
  public.profiles,
  public.wallets,
  public.operators,
  public.operator_prefixes,
  public.catalog_products,
  public.transactions
from anon, authenticated;

grant select on table
  public.profiles,
  public.wallets,
  public.operators,
  public.catalog_products,
  public.transactions
to authenticated;

-- ---------------------------------------------------------------------------
-- Row Level Security
-- ---------------------------------------------------------------------------

alter table public.profiles enable row level security;
alter table public.wallets enable row level security;
alter table public.operators enable row level security;
alter table public.operator_prefixes enable row level security;
alter table public.catalog_products enable row level security;
alter table public.transactions enable row level security;

drop policy if exists "Users can read their own profile" on public.profiles;
create policy "Users can read their own profile"
  on public.profiles for select
  to authenticated
  using ((select auth.uid()) = id);

drop policy if exists "Users can read their own wallet" on public.wallets;
create policy "Users can read their own wallet"
  on public.wallets for select
  to authenticated
  using ((select auth.uid()) = user_id);

drop policy if exists "Users can read their own transactions" on public.transactions;
create policy "Users can read their own transactions"
  on public.transactions for select
  to authenticated
  using ((select auth.uid()) = user_id);

drop policy if exists "Authenticated users can read active operators" on public.operators;
create policy "Authenticated users can read active operators"
  on public.operators for select
  to authenticated
  using (is_active);

drop policy if exists "Authenticated users can read active catalog products" on public.catalog_products;
create policy "Authenticated users can read active catalog products"
  on public.catalog_products for select
  to authenticated
  using (is_active);

-- operator_prefixes intentionally has no policies: server-side validation data only.
