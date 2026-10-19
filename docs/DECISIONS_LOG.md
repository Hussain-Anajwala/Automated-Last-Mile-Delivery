# Decisions Log

Format: ID — date — context — options — decision — rationale — consequences.

---

## D1 — 2026-10-19 — Do not use the native PostgreSQL service on port 5432

- **Context:** The audit found the user's own Windows service `postgresql-x64-16` listening on 5432.
- **Options:** (a) stop/reuse the user's service; (b) run project PostgreSQL in Docker Compose on a different or unpublished host port.
- **Decision:** (b). The Compose PostgreSQL container will not publish host port 5432; if host access is needed for development it will use `15432`.
- **Rationale:** Avoids altering the user's existing database and keeps the approved Compose topology reproducible.
- **Consequences:** Documented connection strings use the Compose service name inside the network and `localhost:15432` from the host.

## D2 — 2026-10-19 — Install JDK 21 user-locally rather than use JDK 22

- **Context:** Only JDK 22.0.2 (non-LTS, no longer updated) is installed; the approved stack is Java 21 LTS.
- **Options:** (a) build with JDK 22 targeting release 21; (b) install a JDK 21 LTS distribution without admin rights.
- **Decision:** (b), Eclipse Temurin 21 ZIP in a user-owned folder (see `docs/BLOCKERS.md` B1), executed in Phase 3.
- **Rationale:** Matches the approved stack and the JDK used by Docker images and Jenkins, avoiding toolchain drift.
- **Consequences:** Builds set `JAVA_HOME` to the JDK 21 folder; JDK 22 stays installed but unused by the project.

## D3 — 2026-10-19 — Use the existing public GitHub remote

- **Context:** `origin` points to an existing, empty, public repository owned by the user.
- **Decision:** Use it as the project repository (CLAUDE.md: use the configured remote; do not create a replacement). No visibility change.
- **Consequences:** Everything pushed is public, so secret scanning before every push is mandatory.

## D4 — 2026-10-19 — Claude Code runs on Windows, not WSL2

- **Context:** This session runs in the VS Code extension on Windows (Git Bash + PowerShell), while the plan *prefers* Claude Code in WSL2 Ubuntu.
- **Decision:** Continue on Windows for application, Maven and Docker work; run Ansible through `wsl -d <Ubuntu distro> -- ...` once B3 is resolved, labelling those commands as Ubuntu/WSL.
- **Consequences:** Documentation provides both `mvnw.cmd` (PowerShell) and `./mvnw` (Ubuntu/WSL) commands.

## D5 — 2026-10-19 — Provisional defaults for teacher questions

- **Context:** `docs/BLOCKERS.md` B6 lists questions only the teacher can answer.
- **Decision:** Until answered: local deployment URL; seeded accounts plus admin-only read-only user list; Dockerized Spring Boot JAR behind Nginx; Ansible target per B4 option A unless the user/teacher chooses B.
- **Consequences:** Revisit if the teacher answers differently; affected phases will be noted.

## D6 — 2026-10-19 — ADMIN status changes follow the normal state machine

- **Context:** PRD §8 / Backend_Schema §7 allow an ADMIN status change only as "explicit audited override, if allowed".
- **Options:** (a) build an override that can bypass the state machine; (b) let ADMIN perform the same valid transitions as DISPATCHER, all audited; (c) forbid ADMIN status changes.
- **Decision:** (b). No bypass of the state machine exists for any role.
- **Rationale:** Keeps one transition policy, satisfies "audited" (every change writes history), and avoids an un-reviewed override path.
- **Consequences:** Role matrix tests cover ADMIN = DISPATCHER for transitions.

## D7 — 2026-10-19 — Assignment events and status history

- **Context:** App_Flow §4 says a separate assignment-event table is optional and needs scope approval.
- **Decision:** No extra table. Assigning a `CREATED` or `FAILED` delivery performs the `→ ASSIGNED` transition and writes a history row (reason records the agent). Reassigning an already `ASSIGNED` delivery updates `assigned_agent_id` only and writes a history row `ASSIGNED → ASSIGNED` with reason `Reassigned to <agent>`, so reassignment is still audited. Reassignment is not allowed for `OUT_FOR_DELIVERY` or `DELIVERED`.
- **Rationale:** Uses only the approved three tables while keeping every operational change auditable.
- **Consequences:** History rows may have `previous_status = new_status = ASSIGNED`; the transition policy treats this as an assignment record, not a state change.

## D8 — 2026-10-19 — Failure metadata on retry

- **Decision:** On `FAILED → ASSIGNED`, `deliveries.failure_reason` is cleared; the original reason remains in `delivery_status_history`. `delivered_at` is set only on `→ DELIVERED`.
- **Rationale:** Backend_Schema §4 asks for a documented clear/retain policy; history already preserves the reason.

## D9 — 2026-10-19 — API authentication

- **Decision:** `/api/**` accepts the browser session or HTTP Basic, and ignores CSRF (stateless clients cannot carry the token). All other routes use form login with CSRF.
- **Rationale:** Keeps documented JSON endpoints scriptable for tests/health without weakening CSRF for browser forms (TRD §6 forbids disabling CSRF globally).
- **Consequences:** Basic credentials must only travel over the local/trusted network; documented as an MVP limitation.

## D10 — 2026-10-19 — Jenkins topology: container on Docker Desktop

- **Options:** native Windows service vs. container.
- **Decision:** Custom image `FROM jenkins/jenkins:2.580.1-lts-jdk21` with Docker CLI, Compose and Chromium, bound to `127.0.0.1:8090`, Docker socket mounted, SCM polling.
- **Rationale:** Reproducible as code, Linux toolchain parity with the deploy target, no Windows service install (no admin).
- **Consequences:** Socket mount is root-equivalent; acceptable only because Jenkins is local and single-user. Execution blocked until Docker runs (B2).

## D11 — 2026-10-19 — Integration and Selenium tests use embedded PostgreSQL

- **Context:** TRD §1 allows Testcontainers "only if justified and workable on the target workstation". Docker is not running (B2), and Jenkins-in-Docker would need Docker-in-Docker for Testcontainers.
- **Decision:** `io.zonky.test:embedded-postgres` 2.2.2 with PostgreSQL 17.11 binaries for tests; runtime uses `postgres:17.11-alpine`.
- **Rationale:** Real PostgreSQL (same version) without Docker; proven in the Phase 3 spike (G3).
- **Consequences:** Tests need ~30 MB of binaries per OS. Embedded PostgreSQL cannot run as root (Jenkins runs as the `jenkins` user, so this is fine).

## D12 — 2026-10-19 — Continue Phases 4–6 while Phase 3 infrastructure gates are blocked

- **Context:** Implementation_Plan §8 says a BLOCKED phase does not authorize dependent phases without an explicit user exception. Phase 3 gates G5–G9 are blocked on user actions B2–B4.
- **Decision:** The user's continuation directive (2026-10-19) explicitly instructs continuing all independent work, including source code and tests, while infrastructure is unavailable. Phases 4–6 depend only on G1–G4 (passed). Phases 7, 8, 10–14 remain blocked until their gates pass.
- **Consequences:** Phase 3 is recorded as `PARTIAL / BLOCKED (infrastructure)`, never as PASS, until G5–G9 are proven.

## D13 — 2026-10-19 — Stack Phase 6 branches on the unmerged Phase 5 branch

- **Context:** The Phase 5 PR cannot be opened until `gh` is authenticated (B5). Merging `feature/delivery-create` into `develop` without a PR would skip the teacher-required PR/review evidence.
- **Decision:** Do not merge without the PR. Phase 6 feature branches start from `feature/delivery-create` (stacked branches). When B5 is resolved, PRs are opened in order (Phase 5 first); each later PR then shows only its own changes.
- **Consequences:** `develop` lags until PRs merge. Phase 5 stays `BLOCKED (PR/review/merge)` with implementation evidence recorded; Phase 6 may proceed under D12.

## D9 (revised) — 2026-10-19 — API is stateless HTTP Basic only

- **Context:** The original D9 let `/api/**` accept either the browser session or HTTP Basic while ignoring CSRF. While implementing the API filter chain this was found to be unsafe: a cross-site request could ride the browser session cookie on a CSRF-exempt endpoint.
- **Decision:** `/api/**` uses a separate stateless filter chain with HTTP Basic only and a request-scoped security context, so session cookies are ignored there. CSRF stays enabled for every browser route.
- **Evidence:** `DeliveryApiTest.browserSessionCookiesAreNotAcceptedByTheApi` (a logged-in session posting to the API gets 401 and nothing is created).

## D14 — 2026-10-19 — Optional optimistic-concurrency token

- **Context:** Backend_Schema §8 forbids client-supplied version values "unless a deliberately documented concurrency protocol requires a token". App_Flow §5 asks that concurrent updates not silently overwrite each other.
- **Decision:** Status, assignment and edit requests accept an optional `expectedVersion` (`version` hidden field in web forms). If it differs from the stored `@Version`, the request is rejected with 409 and nothing changes. It is only compared, never written.
- **Consequences:** Double submissions and stale pages cannot apply a transition twice (`DeliveryWorkflowServiceTest.staleVersionIsRejected…`).

## D15 — 2026-10-19 — PR review policy until the teacher answers Q2

- **Context:** The teacher has not said whether a second person must review PRs (BLOCKERS B6 Q2). Implementation_Plan §5 (Phase 5) describes "open PR, review the diff, address findings, merge"; USER_SETUP_CHECKLIST treats an independent reviewer as needed only *if the course requires it*.
- **Decision (provisional):** PRs #8–#15 were merged after a documented author self-review performed by the Claude Code assistant, labelled in every PR as **not an independent review**. No approval was faked (GitHub does not allow approving one's own PR).
- **Consequences:** If the teacher requires an independent reviewer, a classmate/teacher reviews the remaining open PR(s) (e.g. the Phase 15 documentation PR) and may add post-merge review comments on #8/#9; the evidence index states this gap explicitly.

## D16 — 2026-10-19 — Only `main` deploys

- **Context:** After merging the stack, `develop` and the feature branch `ci/jenkins-pipeline` also deployed to the single local environment (review finding).
- **Decision:** `DEPLOY_BRANCHES = ['main']`; other branches build, test and publish images (PR #14). Verified: `develop` #1/#2 logged `Stage "Deploy" skipped due to when conditional`.

## D17 — 2026-10-19 — One Jenkins executor

- **Context:** Two WSL VM crashes from commit-limit exhaustion; two executors could run two heavy pipelines at once.
- **Decision:** `numExecutors: 1` (JCasC). Jenkins data persisted across the container recreation (job history intact).
