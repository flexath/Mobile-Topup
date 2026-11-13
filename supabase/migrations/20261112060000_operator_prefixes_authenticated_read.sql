-- Allow authenticated clients to cache operator prefixes offline for detection.
-- Writes remain revoked; process_recharge still validates against this table server-side.

comment on table public.operator_prefixes is
  'PRD common-prefix dataset. Readable by authenticated clients for offline Room cache; process_recharge validates against this table.';

grant select on table public.operator_prefixes to authenticated;

drop policy if exists "Authenticated users can read operator prefixes" on public.operator_prefixes;
create policy "Authenticated users can read operator prefixes"
  on public.operator_prefixes for select
  to authenticated
  using (true);
