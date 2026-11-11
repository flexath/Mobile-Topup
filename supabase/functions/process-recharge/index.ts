// process-recharge — simulated mobile recharge for the TopUp Android app.
//
// The only authorized way to deduct a wallet balance and create a successful transaction.
// No telecom provider is contacted: a "successful" recharge means the simulated wallet was charged.
//
// Request (POST, Authorization: Bearer <user access token>):
//   { "phone_number": "09xxxxxxxxx", "operator_code": "MPT", "product_id": "<uuid>" , "idempotency_key": "<uuid>" }
//   or replace product_id with "custom_amount_mmk": <int>.
//
// The caller's identity is resolved from the access token on the Auth server; the body never
// carries a user id. Price, balance and operator validation happen in public.process_recharge,
// which runs atomically in Postgres and is executable by service_role only.

import { createClient } from "npm:@supabase/supabase-js@2.119.0";

const PHONE_PATTERN = /^09\d{9}$/;
const UUID_PATTERN = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
const OPERATOR_CODES = new Set(["MPT", "ATOM", "OOREDOO", "MYTEL"]);
const MAX_BODY_BYTES = 4_096;

const ERROR_STATUS: Record<string, number> = {
  INVALID_REQUEST: 400,
  INVALID_PHONE_NUMBER: 400,
  INVALID_AMOUNT: 400,
  UNAUTHORIZED: 401,
  INSUFFICIENT_BALANCE: 402,
  WALLET_NOT_FOUND: 404,
  METHOD_NOT_ALLOWED: 405,
  IDEMPOTENCY_KEY_REUSED: 409,
  UNSUPPORTED_OPERATOR: 422,
  OPERATOR_MISMATCH: 422,
  PRODUCT_NOT_AVAILABLE: 422,
  PRODUCT_OPERATOR_MISMATCH: 422,
  INTERNAL_ERROR: 500,
};

const ERROR_MESSAGES: Record<string, string> = {
  INVALID_REQUEST: "The recharge request is invalid.",
  INVALID_PHONE_NUMBER: "Enter a valid Myanmar mobile number (09xxxxxxxxx).",
  INVALID_AMOUNT: "The custom amount is outside the allowed range.",
  UNAUTHORIZED: "Your session has expired. Please sign in again.",
  INSUFFICIENT_BALANCE: "Your wallet balance is not enough for this recharge.",
  WALLET_NOT_FOUND: "No wallet was found for this account.",
  METHOD_NOT_ALLOWED: "Use POST to submit a recharge.",
  IDEMPOTENCY_KEY_REUSED: "This request id was already used for a different recharge.",
  UNSUPPORTED_OPERATOR: "This mobile number's operator is not supported.",
  OPERATOR_MISMATCH: "The mobile number does not belong to the selected operator.",
  PRODUCT_NOT_AVAILABLE: "The selected package is no longer available.",
  PRODUCT_OPERATOR_MISMATCH: "The selected package does not belong to this operator.",
  INTERNAL_ERROR: "The recharge could not be processed. Please try again.",
};

const JSON_HEADERS = {
  "Content-Type": "application/json",
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type, x-region",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
};

type RechargeRequest = {
  phoneNumber: string;
  operatorCode: string;
  productId: string | null;
  customAmountMmk: number | null;
  idempotencyKey: string;
};

type RpcResult =
  | { ok: true; replayed: boolean; transaction: Record<string, unknown>; wallet_balance_mmk: number }
  | { ok: false; code: string; [detail: string]: unknown };

function serverKey(): string {
  const secretKeys = Deno.env.get("SUPABASE_SECRET_KEYS");
  if (secretKeys) {
    try {
      const parsed = JSON.parse(secretKeys) as Record<string, string>;
      if (parsed.default) return parsed.default;
    } catch {
      // Fall back to the legacy key below.
    }
  }
  const legacy = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY");
  if (legacy) return legacy;
  throw new Error("Server credentials are not configured");
}

const admin = createClient(Deno.env.get("SUPABASE_URL") ?? "", serverKey(), {
  auth: { persistSession: false, autoRefreshToken: false, detectSessionInUrl: false },
});

function json(status: number, body: unknown): Response {
  return new Response(JSON.stringify(body), { status, headers: JSON_HEADERS });
}

function failure(code: string, details: Record<string, unknown> = {}): Response {
  return json(ERROR_STATUS[code] ?? 500, {
    success: false,
    error: { code, message: ERROR_MESSAGES[code] ?? ERROR_MESSAGES.INTERNAL_ERROR, ...details },
  });
}

function bearerToken(req: Request): string | null {
  const header = req.headers.get("Authorization") ?? "";
  const match = header.match(/^Bearer\s+(.+)$/i);
  return match ? match[1].trim() : null;
}

function parseRequest(raw: unknown): RechargeRequest | { error: string } {
  if (typeof raw !== "object" || raw === null || Array.isArray(raw)) return { error: "INVALID_REQUEST" };
  const body = raw as Record<string, unknown>;

  const phoneNumber = body.phone_number;
  if (typeof phoneNumber !== "string" || !PHONE_PATTERN.test(phoneNumber)) return { error: "INVALID_PHONE_NUMBER" };

  const operatorCode = body.operator_code;
  if (typeof operatorCode !== "string" || !OPERATOR_CODES.has(operatorCode)) return { error: "UNSUPPORTED_OPERATOR" };

  const productId = body.product_id ?? null;
  const customAmount = body.custom_amount_mmk ?? null;
  if ((productId === null) === (customAmount === null)) return { error: "INVALID_REQUEST" };
  if (productId !== null && (typeof productId !== "string" || !UUID_PATTERN.test(productId))) {
    return { error: "INVALID_REQUEST" };
  }
  if (customAmount !== null && (typeof customAmount !== "number" || !Number.isInteger(customAmount) || customAmount <= 0)) {
    return { error: "INVALID_AMOUNT" };
  }

  const idempotencyKey = body.idempotency_key ?? crypto.randomUUID();
  if (typeof idempotencyKey !== "string" || !UUID_PATTERN.test(idempotencyKey)) return { error: "INVALID_REQUEST" };

  return {
    phoneNumber,
    operatorCode,
    productId: productId as string | null,
    customAmountMmk: customAmount as number | null,
    idempotencyKey: idempotencyKey.toLowerCase(),
  };
}

Deno.serve(async (req: Request): Promise<Response> => {
  if (req.method === "OPTIONS") return new Response(null, { status: 204, headers: JSON_HEADERS });
  if (req.method !== "POST") return failure("METHOD_NOT_ALLOWED");

  const token = bearerToken(req);
  if (!token) return failure("UNAUTHORIZED");

  const { data: userData, error: userError } = await admin.auth.getUser(token);
  if (userError || !userData?.user) return failure("UNAUTHORIZED");

  const rawBody = await req.text();
  if (rawBody.length > MAX_BODY_BYTES) return failure("INVALID_REQUEST");

  let parsedBody: unknown;
  try {
    parsedBody = JSON.parse(rawBody);
  } catch {
    return failure("INVALID_REQUEST");
  }

  const request = parseRequest(parsedBody);
  if ("error" in request) return failure(request.error);

  const { data, error } = await admin.rpc("process_recharge", {
    p_user_id: userData.user.id,
    p_idempotency_key: request.idempotencyKey,
    p_phone_number: request.phoneNumber,
    p_operator_code: request.operatorCode,
    p_product_id: request.productId,
    p_custom_amount_mmk: request.customAmountMmk,
  });

  if (error || !data) {
    console.error("process_recharge failed", { code: error?.code ?? "NO_DATA" });
    return failure("INTERNAL_ERROR");
  }

  const result = data as RpcResult;
  if (!result.ok) {
    const { ok: _ok, code, ...details } = result;
    return failure(code, details);
  }

  return json(200, {
    success: true,
    replayed: result.replayed,
    transaction: result.transaction,
    wallet: { balance_mmk: result.wallet_balance_mmk, currency: "MMK" },
  });
});
