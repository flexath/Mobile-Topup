#!/usr/bin/env bash
# End-to-end checks for the deployed process-recharge Edge Function, RLS and catalog API.
#
# Required environment:
#   SUPABASE_URL, SUPABASE_ANON_KEY          project URL + client (anon / publishable) key
#   E2E_EMAIL_A, E2E_EMAIL_B, E2E_PASSWORD   two test users that start at 500,000 MMK
#
# The script spends user A's wallet. Use throwaway accounts only.
set -euo pipefail

: "${SUPABASE_URL:?}" "${SUPABASE_ANON_KEY:?}" "${E2E_EMAIL_A:?}" "${E2E_EMAIL_B:?}" "${E2E_PASSWORD:?}"

FUNCTION_URL="$SUPABASE_URL/functions/v1/process-recharge"
PASS=0
FAIL=0
WORK_DIR="$(mktemp -d)"
trap 'rm -rf "$WORK_DIR"' EXIT

check() {
  local name="$1" condition="$2"
  if [[ "$condition" == "true" ]]; then
    PASS=$((PASS + 1)); echo "PASS  $name"
  else
    FAIL=$((FAIL + 1)); echo "FAIL  $name"
  fi
}

# json_get <python expression over the parsed JSON `d`>
json_get() { python3 -c "import json,sys; d=json.load(sys.stdin); print(eval(sys.argv[1]))" "$1"; }
uuid() { python3 -c "import uuid; print(uuid.uuid4())"; }

sign_in() {
  curl -s "$SUPABASE_URL/auth/v1/token?grant_type=password" \
    -H "apikey: $SUPABASE_ANON_KEY" -H "Content-Type: application/json" \
    -d "{\"email\":\"$1\",\"password\":\"$E2E_PASSWORD\"}" | json_get "d['access_token']"
}

rest() { # rest <token> <path> [curl args...]
  local token="$1" path="$2"; shift 2
  curl -s "$SUPABASE_URL/rest/v1/$path" -H "apikey: $SUPABASE_ANON_KEY" -H "Authorization: Bearer $token" "$@"
}

recharge() { # recharge <token> <json body>  -> writes body to stdout, status to $WORK_DIR/status
  curl -s -o "$WORK_DIR/body" -w "%{http_code}" "$FUNCTION_URL" \
    -H "apikey: $SUPABASE_ANON_KEY" -H "Authorization: Bearer $1" -H "Content-Type: application/json" \
    -d "$2" > "$WORK_DIR/status"
  cat "$WORK_DIR/body"
}
status() { cat "$WORK_DIR/status"; }
fire() { # fire <token> <body file> -> prints HTTP status
  curl -s -o /dev/null -w '%{http_code}\n' "$FUNCTION_URL" \
    -H "apikey: $SUPABASE_ANON_KEY" -H "Authorization: Bearer $1" -H "Content-Type: application/json" -d @"$2"
}

TOKEN_A="$(sign_in "$E2E_EMAIL_A")"
TOKEN_B="$(sign_in "$E2E_EMAIL_B")"
check "users can sign in with email/password" "$([[ -n "$TOKEN_A" && -n "$TOKEN_B" ]] && echo true || echo false)"

BALANCE_A="$(rest "$TOKEN_A" "wallets?select=balance_mmk" | json_get "d[0]['balance_mmk']")"
check "wallet starts at 500,000 MMK" "$([[ "$BALANCE_A" == "500000" ]] && echo true || echo false)"

WALLET_ROWS_A="$(rest "$TOKEN_A" "wallets?select=user_id" | json_get "len(d)" )"
check "user sees exactly one wallet (own)" "$([[ "$WALLET_ROWS_A" == "1" ]] && echo true || echo false)"

CATALOG="$(rest "$TOKEN_A" "operators?select=code,logo_path,catalog_products(id,sku,price_mmk)&order=sort_order")"
OPERATORS="$(echo "$CATALOG" | json_get "len(d)")"
PRODUCTS="$(echo "$CATALOG" | json_get "sum(len(o['catalog_products']) for o in d)")"
check "unified catalog returns 4 operators / 44 products in one request" "$([[ "$OPERATORS" == "4" && "$PRODUCTS" == "44" ]] && echo true || echo false)"

ANON_CATALOG_STATUS="$(curl -s -o /dev/null -w "%{http_code}" "$SUPABASE_URL/rest/v1/catalog_products?select=id" -H "apikey: $SUPABASE_ANON_KEY")"
check "anonymous catalog access denied ($ANON_CATALOG_STATUS)" "$([[ "$ANON_CATALOG_STATUS" == "401" || "$ANON_CATALOG_STATUS" == "403" ]] && echo true || echo false)"

PRODUCT_1000="$(echo "$CATALOG" | json_get "[p['id'] for o in d for p in o['catalog_products'] if p['sku']=='MPT-AIRTIME-1000'][0]")"
PRODUCT_20000="$(echo "$CATALOG" | json_get "[p['id'] for o in d for p in o['catalog_products'] if p['sku']=='MPT-AIRTIME-20000'][0]")"
PRODUCT_30000="$(echo "$CATALOG" | json_get "[p['id'] for o in d for p in o['catalog_products'] if p['sku']=='MPT-AIRTIME-30000'][0]")"

# --- Authentication ------------------------------------------------------------
NO_TOKEN_STATUS="$(curl -s -o /dev/null -w "%{http_code}" "$FUNCTION_URL" -H "apikey: $SUPABASE_ANON_KEY" -H "Content-Type: application/json" -d '{}')"
check "missing session -> 401" "$([[ "$NO_TOKEN_STATUS" == "401" ]] && echo true || echo false)"
recharge "not-a-jwt" "{}" > /dev/null
check "invalid session -> 401" "$([[ "$(status)" == "401" ]] && echo true || echo false)"
recharge "$SUPABASE_ANON_KEY" "{\"phone_number\":\"09420000001\",\"operator_code\":\"MPT\",\"product_id\":\"$PRODUCT_1000\"}" > /dev/null
check "anon key instead of user session -> 401" "$([[ "$(status)" == "401" ]] && echo true || echo false)"

# --- Success + idempotency -------------------------------------------------------
KEY="$(uuid)"
BODY="{\"phone_number\":\"09420000001\",\"operator_code\":\"MPT\",\"product_id\":\"$PRODUCT_1000\",\"idempotency_key\":\"$KEY\",\"amount_mmk\":1,\"user_id\":\"00000000-0000-0000-0000-000000000000\"}"
RESULT="$(recharge "$TOKEN_A" "$BODY")"
check "successful recharge -> 200, authoritative price, balance 499,000 (client price/user id ignored)" \
  "$([[ "$(status)" == "200" && "$(echo "$RESULT" | json_get "d['wallet']['balance_mmk']")" == "499000" && "$(echo "$RESULT" | json_get "d['transaction']['amount_mmk']")" == "1000" ]] && echo true || echo false)"
TX_ID="$(echo "$RESULT" | json_get "d['transaction']['id']")"

RESULT="$(recharge "$TOKEN_A" "$BODY")"
check "retry with same idempotency key replays the same transaction" \
  "$([[ "$(status)" == "200" && "$(echo "$RESULT" | json_get "d['replayed']")" == "True" && "$(echo "$RESULT" | json_get "d['transaction']['id']")" == "$TX_ID" && "$(echo "$RESULT" | json_get "d['wallet']['balance_mmk']")" == "499000" ]] && echo true || echo false)"

# --- Validation ------------------------------------------------------------------
recharge "$TOKEN_A" "{\"phone_number\":\"0942000\",\"operator_code\":\"MPT\",\"product_id\":\"$PRODUCT_1000\"}" > /dev/null
check "invalid phone -> 400" "$([[ "$(status)" == "400" ]] && echo true || echo false)"
RESULT="$(recharge "$TOKEN_A" "{\"phone_number\":\"09790000001\",\"operator_code\":\"MPT\",\"product_id\":\"$PRODUCT_1000\"}")"
check "prefix/operator mismatch -> 422 OPERATOR_MISMATCH" "$([[ "$(status)" == "422" && "$(echo "$RESULT" | json_get "d['error']['code']")" == "OPERATOR_MISMATCH" ]] && echo true || echo false)"
RESULT="$(recharge "$TOKEN_A" "{\"phone_number\":\"09420000001\",\"operator_code\":\"MPT\",\"product_id\":\"$(uuid)\"}")"
check "unknown product -> 422 PRODUCT_NOT_AVAILABLE" "$([[ "$(status)" == "422" && "$(echo "$RESULT" | json_get "d['error']['code']")" == "PRODUCT_NOT_AVAILABLE" ]] && echo true || echo false)"
recharge "$TOKEN_A" "{\"phone_number\":\"09420000001\",\"operator_code\":\"MPT\",\"product_id\":\"$PRODUCT_1000\",\"custom_amount_mmk\":1000}" > /dev/null
check "product and custom amount together -> 400" "$([[ "$(status)" == "400" ]] && echo true || echo false)"

# --- Direct client writes are blocked --------------------------------------------
RPC_STATUS="$(rest "$TOKEN_A" "rpc/process_recharge" -o /dev/null -w "%{http_code}" -X POST -H "Content-Type: application/json" \
  -d "{\"p_user_id\":\"$(uuid)\",\"p_idempotency_key\":\"$(uuid)\",\"p_phone_number\":\"09420000001\",\"p_operator_code\":\"MPT\",\"p_custom_amount_mmk\":1000}")"
check "client cannot call process_recharge RPC directly ($RPC_STATUS)" "$([[ "$RPC_STATUS" == "401" || "$RPC_STATUS" == "403" || "$RPC_STATUS" == "404" ]] && echo true || echo false)"
PATCH_STATUS="$(rest "$TOKEN_A" "wallets?user_id=not.is.null" -o /dev/null -w "%{http_code}" -X PATCH -H "Content-Type: application/json" -d '{"balance_mmk":999999999}')"
check "client cannot update wallet ($PATCH_STATUS)" "$([[ "$PATCH_STATUS" == "401" || "$PATCH_STATUS" == "403" ]] && echo true || echo false)"
INSERT_STATUS="$(rest "$TOKEN_A" "transactions" -o /dev/null -w "%{http_code}" -X POST -H "Content-Type: application/json" -d '{"phone_number":"09420000001"}')"
check "client cannot insert transactions ($INSERT_STATUS)" "$([[ "$INSERT_STATUS" == "401" || "$INSERT_STATUS" == "403" ]] && echo true || echo false)"

# --- RLS isolation ----------------------------------------------------------------
B_SEES="$(rest "$TOKEN_B" "transactions?select=id&id=eq.$TX_ID" | json_get "len(d)")"
check "user B cannot read user A's transaction" "$([[ "$B_SEES" == "0" ]] && echo true || echo false)"
B_BALANCE="$(rest "$TOKEN_B" "wallets?select=balance_mmk" | json_get "[w['balance_mmk'] for w in d]")"
check "user B sees only own untouched wallet" "$([[ "$B_BALANCE" == "[500000]" ]] && echo true || echo false)"

# --- Concurrency: 20 parallel 30,000 MMK recharges from 499,000 -------------------
for i in $(seq 1 20); do
  echo "{\"phone_number\":\"09420000001\",\"operator_code\":\"MPT\",\"product_id\":\"$PRODUCT_30000\",\"idempotency_key\":\"$(uuid)\"}" > "$WORK_DIR/req_$i.json"
done
for i in $(seq 1 20); do fire "$TOKEN_A" "$WORK_DIR/req_$i.json" > "$WORK_DIR/concurrent_$i" & done
wait
cat "$WORK_DIR"/concurrent_* > "$WORK_DIR/concurrent"
OK_COUNT="$(grep -c '^200$' "$WORK_DIR/concurrent" || true)"
INSUFFICIENT_COUNT="$(grep -c '^402$' "$WORK_DIR/concurrent" || true)"
BALANCE_A="$(rest "$TOKEN_A" "wallets?select=balance_mmk" | json_get "d[0]['balance_mmk']")"
check "concurrent recharges never overspend (200=$OK_COUNT, 402=$INSUFFICIENT_COUNT, balance=$BALANCE_A)" \
  "$([[ "$OK_COUNT" == "16" && "$INSUFFICIENT_COUNT" == "4" && "$BALANCE_A" == "19000" ]] && echo true || echo false)"

# --- Concurrency: 10 parallel retries of one idempotency key ----------------------
DUP_KEY="$(uuid)"
echo "{\"phone_number\":\"09420000001\",\"operator_code\":\"MPT\",\"product_id\":\"$PRODUCT_1000\",\"idempotency_key\":\"$DUP_KEY\"}" > "$WORK_DIR/dup.json"
for i in $(seq 1 10); do fire "$TOKEN_A" "$WORK_DIR/dup.json" > "$WORK_DIR/dup_$i" & done
wait
cat "$WORK_DIR"/dup_* > "$WORK_DIR/dup"
DUP_OK="$(grep -c '^200$' "$WORK_DIR/dup" || true)"
BALANCE_A="$(rest "$TOKEN_A" "wallets?select=balance_mmk" | json_get "d[0]['balance_mmk']")"
check "parallel duplicate requests charge once (200=$DUP_OK, balance=$BALANCE_A)" \
  "$([[ "$DUP_OK" == "10" && "$BALANCE_A" == "18000" ]] && echo true || echo false)"

# --- Insufficient balance ----------------------------------------------------------
TX_COUNT_BEFORE="$(rest "$TOKEN_A" "transactions?select=id" | json_get "len(d)")"
RESULT="$(recharge "$TOKEN_A" "{\"phone_number\":\"09420000001\",\"operator_code\":\"MPT\",\"product_id\":\"$PRODUCT_20000\",\"idempotency_key\":\"$(uuid)\"}")"
TX_COUNT_AFTER="$(rest "$TOKEN_A" "transactions?select=id" | json_get "len(d)")"
BALANCE_A="$(rest "$TOKEN_A" "wallets?select=balance_mmk" | json_get "d[0]['balance_mmk']")"
check "insufficient balance -> 402, no deduction, no history row" \
  "$([[ "$(status)" == "402" && "$(echo "$RESULT" | json_get "d['error']['code']")" == "INSUFFICIENT_BALANCE" && "$BALANCE_A" == "18000" && "$TX_COUNT_BEFORE" == "$TX_COUNT_AFTER" ]] && echo true || echo false)"

# --- History RPC -------------------------------------------------------------------
PAGE_1="$(rest "$TOKEN_A" "rpc/list_my_transactions" -X POST -H "Content-Type: application/json" -d '{"p_limit":10,"p_offset":0}')"
PAGE_2="$(rest "$TOKEN_A" "rpc/list_my_transactions" -X POST -H "Content-Type: application/json" -d '{"p_limit":10,"p_offset":10}')"
check "history pagination: 18 rows split 10 + 8 without overlap" \
  "$(python3 -c "
import json,sys
a=json.loads(sys.argv[1]); b=json.loads(sys.argv[2])
ids=[r['id'] for r in a+b]
print(str(len(a)==10 and len(b)==8 and len(set(ids))==18).lower())" "$PAGE_1" "$PAGE_2")"
SEARCH="$(rest "$TOKEN_A" "rpc/list_my_transactions" -X POST -H "Content-Type: application/json" -d "{\"p_search\":\"${TX_ID:0:8}\"}" | json_get "[r['id'] for r in d]")"
check "history search by partial transaction id" "$([[ "$SEARCH" == "['$TX_ID']" ]] && echo true || echo false)"
OLDEST="$(rest "$TOKEN_A" "rpc/list_my_transactions" -X POST -H "Content-Type: application/json" -d '{"p_oldest_first":true,"p_limit":1}' | json_get "d[0]['id']")"
check "oldest-first sorting returns the first purchase" "$([[ "$OLDEST" == "$TX_ID" ]] && echo true || echo false)"

echo
echo "Passed: $PASS  Failed: $FAIL"
[[ "$FAIL" == "0" ]]
