#!/usr/bin/env bash
set -euo pipefail

base_url="${1:-http://localhost:8080}"

if ! command -v jq >/dev/null 2>&1; then
  echo "jq is required for JSON validation. Please install jq and retry."
  exit 1
fi

account_name="Travel Fund $(date +%s)"
post_payload="$(jq -nc --arg name "$account_name" '{name: $name, currency: "USD", balance: 250.50}')"

root_response="$(curl -fsS "$base_url/")"
accounts_response="$(curl -fsS "$base_url/accounts")"
created_response="$(curl -fsS -X POST "$base_url/accounts" \
  -H 'Content-Type: application/json' \
  -d "$post_payload")"
created_id="$(jq -r '.id // empty' <<<"$created_response")"

if [[ "$root_response" != *"quarkus accounts service is running"* ]]; then
  echo "Root endpoint check failed"
  exit 1
fi

if ! jq -e 'arrays and any(.[]; .name == "Savings")' >/dev/null <<<"$accounts_response"; then
  echo "Accounts endpoint check failed: expected an account named 'Savings'"
  exit 1
fi

if [[ -z "$created_id" ]]; then
  echo "Accounts POST check failed: response did not include an id"
  exit 1
fi

if ! jq -e --arg name "$account_name" '.name == $name and .currency == "USD"' >/dev/null <<<"$created_response"; then
  echo "Accounts POST check failed: response did not include the created account data"
  exit 1
fi

created_lookup_response="$(curl -fsS "$base_url/accounts/$created_id")"
if ! jq -e --arg id "$created_id" --arg name "$account_name" '.id == $id and .name == $name' >/dev/null <<<"$created_lookup_response"; then
  echo "Account lookup check failed for id '$created_id'"
  exit 1
fi

echo "Smoke test passed for $base_url"
echo "Smoke test passed for $base_url/accounts"
echo "Smoke test passed for POST $base_url/accounts"

