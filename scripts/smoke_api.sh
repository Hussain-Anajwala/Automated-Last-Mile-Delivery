#!/usr/bin/env bash
# Live smoke test of the JSON API workflow against a running instance with demo data.
# Usage: scripts/smoke_api.sh <base-url> <file holding the demo password>
# The password file is local only and must never be committed.
set -u
BASE="${1:-http://localhost:18080}"
PW="$(cat "$2")"
DISP="dispatcher@lastmile.test:$PW"
AG1="agent.one@lastmile.test:$PW"
AG2="agent.two@lastmile.test:$PW"
JSON='Content-Type: application/json'

req() { # user method path [body] -> prints "HTTP <code> <body>"
  local user="$1" method="$2" path="$3" body="${4:-}"
  if [ -n "$body" ]; then
    curl -s -u "$user" -X "$method" -H "$JSON" -d "$body" -w ' HTTP %{http_code}' "$BASE$path"
  else
    curl -s -u "$user" -X "$method" -w ' HTTP %{http_code}' "$BASE$path"
  fi
}
field() { python -I -c "import json,sys; d=json.loads(sys.stdin.read().rsplit(' HTTP',1)[0]); print(d$1)"; }

echo "== health";            curl -s -w ' HTTP %{http_code}\n' "$BASE/api/v1/health/ready"
echo "== no credentials";    curl -s -o /dev/null -w 'HTTP %{http_code}\n' "$BASE/api/v1/deliveries"
echo "== dashboard (dispatcher)"; req "$DISP" GET /api/v1/dashboard/summary | field "['total'], dict(sorted(d['countsByStatus'].items()))"
echo "== dashboard (agent.one)";  req "$AG1" GET /api/v1/dashboard/summary | field "['scope'], d['total']"
echo "== search q=ord-200 status=CREATED"; req "$DISP" GET "/api/v1/deliveries?q=ord-200&status=CREATED" | field "['totalElements'], [r['referenceNumber'] for r in d['content']]"

echo "== create";
OUT=$(req "$DISP" POST /api/v1/deliveries '{"recipientName":"Smoke Test","recipientPhone":"9111111111","deliveryAddress":"1 Smoke Road","deliveryCity":"Pune","referenceNumber":"SMOKE-1"}')
echo "${OUT##* HTTP} status=$(echo "$OUT" | field "['status']") code=$(echo "$OUT" | field "['deliveryCode']")"
ID=$(echo "$OUT" | field "['id']")

echo "== invalid transition CREATED->DELIVERED"; req "$DISP" PATCH "/api/v1/deliveries/$ID/status" '{"newStatus":"DELIVERED"}' | tail -c 120; echo
echo "== agent.one reads unassigned delivery"; req "$AG1" GET "/api/v1/deliveries/$ID" | tail -c 60; echo
echo "== assign to agent.one (agent.one is seeded third: id 3; response must name Arjun Agent)";
req "$DISP" PATCH "/api/v1/deliveries/$ID/assignment" '{"agentId":3}' | field "['status'], d['assignedAgentName']"
echo "== agent.two tries to dispatch it"; req "$AG2" PATCH "/api/v1/deliveries/$ID/status" '{"newStatus":"OUT_FOR_DELIVERY"}' | tail -c 60; echo
echo "== agent.one dispatches";      req "$AG1" PATCH "/api/v1/deliveries/$ID/status" '{"newStatus":"OUT_FOR_DELIVERY"}' | field "['status']"
echo "== agent.one fails w/o reason"; req "$AG1" PATCH "/api/v1/deliveries/$ID/status" '{"newStatus":"FAILED","reason":""}' | tail -c 140; echo
echo "== agent.one delivers";        req "$AG1" PATCH "/api/v1/deliveries/$ID/status" '{"newStatus":"DELIVERED"}' | field "['status'], d['deliveredAt'] is not None, [h['newStatus'] for h in d['history']]"
echo "== edit delivered";            req "$DISP" PUT "/api/v1/deliveries/$ID" '{"recipientName":"X","recipientPhone":"9111111111","deliveryAddress":"A","deliveryCity":"B"}' | tail -c 90; echo
echo "== dashboard after (dispatcher)"; req "$DISP" GET /api/v1/dashboard/summary | field "['total'], d['countsByStatus']['DELIVERED']"
