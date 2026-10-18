# User Stories and Acceptance Criteria (Teacher Task 2)

Format: *As a … I want … so that …*, then Given/When/Then criteria. Each story maps to backlog task
(T#) and acceptance ID (A#, `docs/PROBLEM_SCOPE.md` §7). Every criterion names the test layer that
proves it: **U** unit, **I** integration (Spring/DB), **S** Selenium.

## Product stories

### US1 — Sign in by role (T6, A4)
As any staff user I want to sign in with my email and password so that I see only what my role permits.
- Given a valid active account, when I submit correct credentials, then I land on `/dashboard` with my name and role shown. **I, S**
- Given wrong credentials or an inactive account, then I see one generic "Invalid email or password" message and no session is created. **I**
- When I sign out, then the session is invalidated and protected pages redirect to `/login`. **I**
- Unauthenticated JSON requests get `401`, not a login page. **I**

### US2 — Create a delivery (T5, A1, A2)
As a dispatcher I want to create a delivery so that work is recorded centrally.
- Given valid recipient name, phone, address and city, when I save, then a unique code (`LMD-YYYYMMDD-NNNNNN` style), status `CREATED`, creator = me and one `CREATED` history row are persisted and I see the detail page with a success message. **U, I, S**
- Given a missing required field or a phone not matching 7–15 digits (optional leading `+`), then field errors are shown, input is preserved, and no row is saved. **I**
- Client-supplied code/status/creator/timestamps are ignored. **I**
- An AGENT requesting the form or POST receives `403`. **I**

### US3 — Search and filter (T6, A3)
As a dispatcher I want to search by code, recipient name or reference and filter by status/agent so that I find records quickly.
- Search is case-insensitive partial match; filters combine with AND; results paginate (default 10/page) with total count. **I, S**
- No matches shows an empty state with a *Reset filters* action. **I**

### US4 — View and edit a delivery (T6, A1)
As a dispatcher I want to edit permitted fields so that mistakes can be corrected.
- Only recipient, phone, address, city and reference are editable; status/history do not change on edit. **I**
- Editing a `DELIVERED` delivery is rejected (`409`). **U, I**

### US5 — Assign and reassign (T6, A5, A6)
As a dispatcher I want to assign an active agent so that someone owns the delivery.
- Assigning a `CREATED` delivery to an active AGENT sets the agent and transitions to `ASSIGNED` with one history row. **U, I, S**
- Assigning to an inactive user or a non-AGENT is rejected (`400`) with no change. **I**
- Reassigning an `ASSIGNED` delivery changes the agent and adds an audited history row (D7); reassigning `OUT_FOR_DELIVERY`/`DELIVERED` is rejected (`409`). **U, I**
- Assigning a `FAILED` delivery performs the retry `FAILED → ASSIGNED` and clears `failure_reason` (D8). **U, I**

### US6 — Agent views own deliveries (T6, A4)
As an agent I want to see only my assigned deliveries so that I focus on my work and others' data stays private.
- List, detail, API and dashboard include only deliveries where `assigned_agent_id = me`, even if I pass `agentId` for someone else. **I, S**
- Opening another agent's delivery URL returns `404` (no detail leakage). **I, S**

### US7 — Advance delivery status (T6, A5, A6)
As an agent I want to mark my delivery out for delivery, delivered or failed so that the record reflects reality.
- Allowed: `ASSIGNED → OUT_FOR_DELIVERY`, `OUT_FOR_DELIVERY → DELIVERED|FAILED`. Each writes one history row with me as actor; `DELIVERED` sets `delivered_at`. **U, I, S**
- `FAILED` without a non-blank reason is rejected (`400`) and nothing changes. **U, I, S**
- Any other transition (e.g. `CREATED → DELIVERED`, from `DELIVERED`) is rejected (`409`) and nothing changes. **U, I**
- A failure inside the transaction rolls back both status and history. **I**

### US8 — Role-scoped dashboard (T6, A7)
As any user I want summary counts so that I see the operational picture.
- Cards show total and per-status counts computed by database aggregation for my scope. **I**
- Counts change immediately after a status change. **S**

### US9 — Admin user list (T6, AS2)
As an administrator I want a read-only list of accounts with role and active flag so that I can review access.
- `/admin/users` visible and accessible to ADMIN only; others get `403`. **I**

### US10 — Health endpoints (T8, T11, T14)
As a DevOps engineer I want liveness and readiness endpoints so that deployments are verified.
- `/api/v1/health/live` → `200 {"status":"UP"}` while the process runs. **I**
- `/api/v1/health/ready` → `200` only when the database responds; `503` otherwise. **I**

## DevOps stories

| ID | Story | Acceptance | Task |
|---|---|---|---|
| DS1 | As the developer I want a clean checkout to build with the wrapper so builds are reproducible | `mvnw.cmd verify` exit 0 on fresh clone with JDK 21 | T4 |
| DS2 | As the developer I want Jenkins to build every new commit | SCM poll detects commit; console log shows build + tests; artifacts archived | T7 |
| DS3 | As the evaluator I want deployment defined as code | `Jenkinsfile` stages incl. deploy + health; ≥1 parameter | T8 |
| DS4 | As QA I want critical journeys automated in a browser | 3–5 Selenium journeys, headless, screenshots on failure, HTML/JUnit report | T9 |
| DS5 | As the evaluator I want a failing test to block release | Deliberate defect → red pipeline, no deploy; fix → green | T10 |
| DS6 | As ops I want a versioned, non-root, health-checked image | Lifecycle transcript; `docker inspect` shows user + healthcheck | T11 |
| DS7 | As ops I want images published to a private local registry and deployed by tag | Same reference pushed and pulled; deployed version = commit SHA | T12 |
| DS8 | As ops I want the target configured idempotently | Playbook run 1 configures; run 2 `changed=0` | T13, T14 |
| DS9 | As ops I want to roll back to the last good release | Failed candidate → previous tag restored → readiness 200 | T14 |
