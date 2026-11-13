-- TopUp catalog seed.
--
-- * Operators reference the logo files already uploaded to the public `telecom` Storage bucket.
-- * Prefixes are the PRD's initial common-prefix dataset (not an exhaustive numbering plan).
-- * Denominations are the values agreed in the Design Specification.
-- * Data packages are DEMO records (is_demo = true) created only so the simulated app is
--   demonstrable. They are not real operator offers.
-- * Custom-amount bounds (1,000 - 50,000 MMK in steps of 100) are a demo rule enforced by
--   process_recharge and published to clients through the operators table.
--
-- Every statement is an upsert so the migration can be re-run safely.

insert into public.operators (
  code, name, display_name, logo_bucket, logo_path,
  custom_amount_min_mmk, custom_amount_max_mmk, custom_amount_step_mmk, sort_order
) values
  ('MPT', 'Myanma Posts and Telecommunications', 'MPT', 'telecom', 'mpt.jpeg', 1000, 50000, 100, 1),
  ('ATOM', 'ATOM Myanmar', 'ATOM', 'telecom', 'atom.jpeg', 1000, 50000, 100, 2),
  ('OOREDOO', 'Ooredoo Myanmar', 'Ooredoo', 'telecom', 'ooredoo.jpeg', 1000, 50000, 100, 3),
  ('MYTEL', 'Telecom International Myanmar (Mytel)', 'Mytel', 'telecom', 'mytel.jpeg', 1000, 50000, 100, 4)
on conflict (code) do update set
  name = excluded.name,
  display_name = excluded.display_name,
  logo_bucket = excluded.logo_bucket,
  logo_path = excluded.logo_path,
  custom_amount_min_mmk = excluded.custom_amount_min_mmk,
  custom_amount_max_mmk = excluded.custom_amount_max_mmk,
  custom_amount_step_mmk = excluded.custom_amount_step_mmk,
  sort_order = excluded.sort_order;

insert into public.operator_prefixes (prefix, operator_code)
select p.prefix, p.operator_code
from (
  -- MPT: 09 20-25, 09 260-268, 09 40-45, 09 47, 09 49, 09 50-56, 09 73, 09 83-89, 09 91
  select '09' || n::text as prefix, 'MPT' as operator_code from generate_series(20, 25) as n
  union all select '09' || n::text, 'MPT' from generate_series(260, 268) as n
  union all select '09' || n::text, 'MPT' from generate_series(40, 45) as n
  union all select '0947', 'MPT'
  union all select '0949', 'MPT'
  union all select '09' || n::text, 'MPT' from generate_series(50, 56) as n
  union all select '0973', 'MPT'
  union all select '09' || n::text, 'MPT' from generate_series(83, 89) as n
  union all select '0991', 'MPT'
  -- ATOM: 09 75-79
  union all select '09' || n::text, 'ATOM' from generate_series(75, 79) as n
  -- Ooredoo: 09 95-97
  union all select '09' || n::text, 'OOREDOO' from generate_series(95, 97) as n
  -- Mytel: 09 68-69
  union all select '09' || n::text, 'MYTEL' from generate_series(68, 69) as n
) as p
on conflict (prefix) do update set operator_code = excluded.operator_code;

-- Denominations: identical set for every operator.
insert into public.catalog_products (
  sku, operator_code, product_type, name, price_mmk, is_demo, sort_order
)
select
  o.code || '-AIRTIME-' || d.amount,
  o.code,
  'DENOMINATION',
  'Airtime top-up',
  d.amount,
  true,
  d.ordinal
from public.operators o
cross join (
  values (1000, 1), (2000, 2), (3000, 3), (5000, 4), (10000, 5), (20000, 6), (30000, 7)
) as d(amount, ordinal)
on conflict (sku) do update set
  operator_code = excluded.operator_code,
  product_type = excluded.product_type,
  name = excluded.name,
  price_mmk = excluded.price_mmk,
  is_demo = excluded.is_demo,
  sort_order = excluded.sort_order;

-- Demo data packages.
insert into public.catalog_products (
  sku, operator_code, product_type, name, price_mmk,
  data_allowance_mb, call_minutes, services, validity_days, is_demo, sort_order
) values
  ('MPT-DEMO-PKG-STARTER', 'MPT', 'DATA_PACKAGE', 'Starter Data', 1500, 1024, null, '{}', 7, true, 1),
  ('MPT-DEMO-PKG-TALK', 'MPT', 'DATA_PACKAGE', 'Talk & Data', 5000, 2048, 100, '{}', 15, true, 2),
  ('MPT-DEMO-PKG-MONTHLY', 'MPT', 'DATA_PACKAGE', 'Monthly Data', 15000, 10240, null, '{}', 30, true, 3),
  ('MPT-DEMO-PKG-SOCIAL', 'MPT', 'DATA_PACKAGE', 'Social Pack', 20000, 5120, 50, '{YouTube,TikTok}', 30, true, 4),

  ('ATOM-DEMO-PKG-DAILY', 'ATOM', 'DATA_PACKAGE', 'Daily Data', 800, 512, null, '{}', 1, true, 1),
  ('ATOM-DEMO-PKG-WEEKLY', 'ATOM', 'DATA_PACKAGE', 'Weekly Data', 2500, 3072, null, '{}', 7, true, 2),
  ('ATOM-DEMO-PKG-SOCIAL', 'ATOM', 'DATA_PACKAGE', 'Social Pack', 6000, 4096, null, '{Facebook,Viber}', 30, true, 3),
  ('ATOM-DEMO-PKG-COMBO', 'ATOM', 'DATA_PACKAGE', 'Monthly Combo', 18000, 12288, 150, '{}', 30, true, 4),

  ('OOREDOO-DEMO-PKG-STARTER', 'OOREDOO', 'DATA_PACKAGE', 'Starter Data', 1200, 1024, null, '{}', 5, true, 1),
  ('OOREDOO-DEMO-PKG-VIDEO', 'OOREDOO', 'DATA_PACKAGE', 'Video Pack', 7000, 6144, null, '{YouTube,TikTok}', 15, true, 2),
  ('OOREDOO-DEMO-PKG-TALK', 'OOREDOO', 'DATA_PACKAGE', 'Talk & Data', 9000, 5120, 200, '{}', 30, true, 3),
  ('OOREDOO-DEMO-PKG-MONTHLY', 'OOREDOO', 'DATA_PACKAGE', 'Monthly Data', 16000, 15360, null, '{}', 30, true, 4),

  ('MYTEL-DEMO-PKG-DAILY', 'MYTEL', 'DATA_PACKAGE', 'Daily Data', 700, 600, null, '{}', 1, true, 1),
  ('MYTEL-DEMO-PKG-WEEKLY', 'MYTEL', 'DATA_PACKAGE', 'Weekly Data', 3000, 4096, null, '{}', 7, true, 2),
  ('MYTEL-DEMO-PKG-SOCIAL', 'MYTEL', 'DATA_PACKAGE', 'Social Pack', 8000, 6144, 60, '{Facebook,Telegram}', 30, true, 3),
  ('MYTEL-DEMO-PKG-MONTHLY', 'MYTEL', 'DATA_PACKAGE', 'Monthly Data', 20000, 20480, 100, '{}', 30, true, 4)
on conflict (sku) do update set
  operator_code = excluded.operator_code,
  product_type = excluded.product_type,
  name = excluded.name,
  price_mmk = excluded.price_mmk,
  data_allowance_mb = excluded.data_allowance_mb,
  call_minutes = excluded.call_minutes,
  services = excluded.services,
  validity_days = excluded.validity_days,
  is_demo = excluded.is_demo,
  sort_order = excluded.sort_order;
