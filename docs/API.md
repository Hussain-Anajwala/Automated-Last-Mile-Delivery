# JSON API v1

Base path `/api/v1`. The API shares the web UI's services, validation and authorization.

## Authentication

Stateless **HTTP Basic** only (decision D9, revised). Browser session cookies are ignored on
`/api/**`, which is why CSRF tokens are not required there. Use only over the local or trusted
network. Health endpoints are public.

## Endpoints

| Method | Path | Body / query | Access | Success |
|---|---|---|---|---|
| GET | `/health/live` | — | public | 200 `{"status":"UP"}` |
| GET | `/health/ready` | — | public | 200 UP, or 503 `{"status":"DOWN"}` when the DB is unreachable |
| GET | `/deliveries` | `q`, `status`, `agentId`, `page` (0-based), `size` (1–50, default 10) | authenticated; agents are always limited to their own deliveries | 200 page |
| POST | `/deliveries` | `recipientName`, `recipientPhone`, `deliveryAddress`, `deliveryCity`, `referenceNumber?`, `assignedAgentId?` | ADMIN, DISPATCHER | 201 + `Location` |
| GET | `/deliveries/{id}` | — | staff; agents only if assigned | 200 delivery with history |
| PUT | `/deliveries/{id}` | same fields as create (assignment ignored); `?expectedVersion=` optional | ADMIN, DISPATCHER | 200 |
| PATCH | `/deliveries/{id}/assignment` | `{"agentId":3,"expectedVersion":1}` | ADMIN, DISPATCHER | 200 |
| PATCH | `/deliveries/{id}/status` | `{"newStatus":"FAILED","reason":"…","expectedVersion":2}` | role + ownership + state machine | 200 |
| GET | `/dashboard/summary` | — | authenticated (agent: own scope) | 200 |

Code, status, creator, timestamps and version are always server-managed; if the client sends them
they are ignored. `expectedVersion` is an optional optimistic-concurrency token: send the `version`
you last read, and a stale value returns 409 instead of applying a change twice (decision D14).

## Errors

```json
{"status":409,"error":"Conflict","message":"Cannot change status from Created to Delivered.","fieldErrors":[]}
```

| Status | When |
|---|---|
| 400 | Validation failure (`fieldErrors` lists fields), malformed JSON, unknown enum value, missing failure reason |
| 401 | No or invalid credentials |
| 403 | Authenticated but role not permitted |
| 404 | Delivery missing **or** outside your scope (agents cannot probe other agents' records) |
| 409 | Invalid transition, terminal record, reassignment after dispatch, stale `expectedVersion` |
| 500 | Unexpected error, with a safe message only |

## Example session (real output, 2026-10-19)

From `scripts/smoke_api.sh` against a local demo instance (full output:
`docs/evidence/phase-06-mvp/api-smoke-output.txt`):

```text
== create
 201 status=CREATED code=LMD-20261018-000007
== invalid transition CREATED->DELIVERED
{"status":409,"error":"Conflict","message":"Cannot change status from Created to Delivered.","fieldErrors":[]} HTTP 409
== agent.two tries to dispatch it
","message":"Delivery not found.","fieldErrors":[]} HTTP 404
== agent.one delivers
DELIVERED True ['CREATED', 'ASSIGNED', 'OUT_FOR_DELIVERY', 'DELIVERED']
```

```bash
curl -u "dispatcher@lastmile.test:$LASTMILE_DEMO_PASSWORD" -H 'Content-Type: application/json' \
  -d '{"recipientName":"Meera","recipientPhone":"9876543210","deliveryAddress":"12 Lane","deliveryCity":"Pune"}' \
  http://localhost:18080/api/v1/deliveries
```
