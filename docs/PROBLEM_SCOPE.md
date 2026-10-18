# Problem Definition and Scope (Teacher Task 1)

**Project:** Automated Last-Mile Delivery Management Platform (UI label: *LastMile Ops*)
**Status:** Phase 1 deliverable — approved scope baseline, derived from `docs/PRD.md` v1.1
**Date:** 2026-10-19

## 1. Problem statement

Small delivery teams often track last-mile deliveries in spreadsheets, chat messages and
disconnected notes. Statuses become inconsistent, records are duplicated, lookups are slow, it is
unclear who changed what, and nobody has a reliable picture of how many deliveries are pending,
out, delivered or failed.

A second, engineering problem: software that only works on one developer's laptop cannot be
released safely. The project must also demonstrate a repeatable path from a Git commit to a
tested, packaged, deployed and recoverable release.

## 2. Objectives

| # | Objective | Measured by |
|---|---|---|
| O1 | One central record per delivery with create/view/edit/search | Delivery CRUD + search acceptance tests (A1–A3) |
| O2 | Enforce three roles on the server | Authorization tests incl. crafted requests (A4) |
| O3 | Only valid status transitions, each audited | Transition and history tests (A5–A6) |
| O4 | Dashboard counts derived from the database | Dashboard count tests against direct queries (A7) |
| O5 | Repeatable build/test/deploy lifecycle | Jenkins runs, Selenium gate, registry, Ansible, rollback evidence (A8–A14) |
| O6 | Auditable documentation for all 15 teacher tasks | `docs/evidence/INDEX.md` maps each task to evidence (A15) |

## 3. Stakeholders

| Stakeholder | Interest | Involvement |
|---|---|---|
| Administrator (`ADMIN`) | Global oversight; seeded account review | System user |
| Dispatcher (`DISPATCHER`) | Create, edit, assign, search, monitor deliveries | System user (primary) |
| Delivery agent (`AGENT`) | See own assigned deliveries; record outcomes | System user |
| Developer / DevOps engineer (student) | Build, test, package, deploy, recover | Project owner |
| Subject teacher / evaluator | Verify 15 tasks and their evidence | Assessor; source of rubric clarifications |
| Hypothetical small delivery business | Consistent records and outcome visibility | Problem context only (fictional) |

## 4. Roles and data visibility (confirmed from PRD §8 and Backend_Schema §7)

| Capability | ADMIN | DISPATCHER | AGENT |
|---|---|---|---|
| View deliveries | All | All | Only those assigned to self |
| Create / edit non-system fields | Yes | Yes | No |
| Assign / reassign agent | Yes | Yes | No |
| Change status | Valid transitions (see decision D6) | Valid transitions | Valid transitions on own deliveries |
| Dashboard | Global | Operational (all) | Own assignments |
| User list | Read-only list of seeded accounts | No | No |

## 5. Delivery status rules (confirmed)

`CREATED → ASSIGNED → OUT_FOR_DELIVERY → DELIVERED | FAILED`; `FAILED → ASSIGNED` only by an
ADMIN/DISPATCHER retry/reassignment; `DELIVERED` is terminal. `FAILED` requires a reason. Each
successful transition writes one history row (actor, time, previous/new status, reason) in the same
transaction. The actor always comes from the authenticated session.

## 6. Scope

### In scope (MVP)

1. Login/logout; seeded development-only accounts for the three roles.
2. Role-aware navigation and dashboard.
3. Delivery create with server-generated unique code; list, detail, edit of permitted fields.
4. Search (code, recipient name, reference) with status/agent filters and pagination.
5. Assignment/reassignment to an active `AGENT`.
6. Status workflow and append-only status history.
7. Dashboard counts from database queries.
8. Liveness and readiness health endpoints.
9. The DevOps lifecycle required by the 15 teacher tasks.

### Out of scope (future enhancements)

GPS/maps, route optimization, AI assignment/forecasting, customer tracking portal, mobile app,
SMS/WhatsApp/email/push notifications, payments/COD, real courier/ERP integrations, multi-tenancy,
microservices, mandatory public hosting, self-registration and password-reset email, full user
lifecycle management. These are listed again in the final report's *Future enhancements*.

## 7. Acceptance criteria (measurable)

| ID | Criterion | Verification |
|---|---|---|
| A1 | A DISPATCHER can create a delivery with valid input; the response shows a unique generated code and status `CREATED`; an initial history row exists | Service/integration test + Selenium journey 1 |
| A2 | Missing required fields or a malformed phone number is rejected with field messages and nothing is saved | Validation tests (row count unchanged) |
| A3 | Search by code, recipient name or reference returns matching rows; status/agent filters narrow results; no match shows an empty state | Repository/controller tests + Selenium journey 2 |
| A4 | An AGENT receives 403/404 for another agent's delivery via list, detail, status URL and API; AGENT cannot create/edit/assign | Security integration tests + Selenium journey 3 |
| A5 | Every disallowed transition (e.g. `CREATED → DELIVERED`, anything from `DELIVERED`, `FAILED` without reason) is rejected (HTTP 409/400) and status/history are unchanged | Transition-policy unit tests + integration tests + Selenium journey 5 |
| A6 | Every allowed transition adds exactly one history row with the authenticated actor; a forced failure rolls back both | Transactional integration test |
| A7 | Dashboard counts equal `COUNT(*) … GROUP BY status` for the user's scope | Dashboard integration test |
| A8 | `mvnw verify` passes from a clean checkout with the documented JDK | Clean-clone build log |
| A9 | Jenkins builds from `Jenkinsfile`, archives the JAR and test reports | Jenkins console log + artifacts |
| A10 | A failing mandatory test (unit or Selenium) stops the pipeline before image publication/deployment | Failed-build evidence + fixed rerun |
| A11 | A versioned image runs with external config, passes its health check, and shows lifecycle commands | Docker command transcript |
| A12 | The same image reference is pushed to and pulled from the local registry | Registry catalog/tag + pull log |
| A13 | Ansible configures the isolated Ubuntu target; the second run reports `changed=0` (or explains each change) | Two playbook run logs |
| A14 | A previous known-good image is restored and passes readiness | Rollback log + health output |
| A15 | Every teacher task maps to real evidence | `docs/evidence/INDEX.md` |

No performance, availability or load target is claimed; none will be reported without measurement.

## 8. Constraints

- Approved stack is fixed (see `CLAUDE.md`); no stack substitution without a recorded decision.
- Windows 11 Home workstation: 15.8 GB RAM (often < 2 GB free), C: ~9.8 GB free, D: ~855 GB free;
  no Hyper-V Manager; a native PostgreSQL 16 already uses port 5432.
- Local-only registry and Jenkins; nothing exposed publicly.
- Fictional data only; public GitHub repository, so no secrets in Git.
- Semester-scale effort; scope freeze after Phase 1.

## 9. Assumptions log

| ID | Assumption | Status / owner |
|---|---|---|
| AS1 | A local deployment URL plus screenshots satisfies "deployed application URL" | Provisional — teacher to confirm (BLOCKERS B6.1) |
| AS2 | Seeded accounts + admin read-only user list satisfy admin scope | Provisional — teacher (B6.4) |
| AS3 | Dockerized Spring Boot JAR behind Nginx is acceptable (no external Tomcat) | Provisional — teacher (B6.5) |
| AS4 | An isolated Ubuntu VM is the Ansible target; isolated WSL2 distro is the fallback | Provisional — user/teacher (B4, B6.3) |
| AS5 | A real second-person PR review may be required; Claude cannot supply it | Provisional — teacher (B6.2) |
| AS6 | Assignment changes are not status-history events except when they cause `CREATED/FAILED → ASSIGNED` | Decision D7 |
| AS7 | Retrying a `FAILED` delivery clears `failure_reason` on the delivery row; the reason stays in history | Decision D8 |

## 10. Scope ambiguity check (Phase 1 exit gate)

No open question changes the architecture: AS1–AS5 alter only evidence format or optional admin
screens, and each has a documented default. The scope is frozen for Phase 2.
