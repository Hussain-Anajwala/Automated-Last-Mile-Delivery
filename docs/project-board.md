# Project Board, Sprint Plan and Definition of Done (Teacher Task 2)

Durable Kanban board kept in Git because `gh` is not yet authenticated (BLOCKERS B5). When it is,
tickets will be mirrored to GitHub Issues and the issue numbers added here.

**Columns:** `Backlog` → `Ready` (dependencies met, criteria clear) → `In Progress` → `Review`
(PR open / self-review checklist) → `Done` (meets DoD) · `Blocked` (named blocker ID).

## Board

| Ticket | Title | Depends on | Status | Evidence expected |
|---|---|---|---|---|
| T1.1 | Problem statement, stakeholders, objectives | — | Done | `docs/PROBLEM_SCOPE.md` |
| T1.2 | Scope, assumptions, acceptance criteria | T1.1 | Done | `docs/PROBLEM_SCOPE.md` §6–9 |
| T1.3 | 15-task backlog | T1.2 | Done | `docs/backlog.md` |
| T2.1 | User stories with testable criteria | T1 | Done | `docs/user-stories.md` |
| T2.2 | Board, sprint plan, DoD | T2.1 | Done | this file |
| T2.3 | Git workflow and PR policy | T2.1 | Done | `docs/GIT_WORKFLOW.md` |
| T2.4 | DevOps workflow diagram | T2.1 | Done | `docs/DEVOPS_WORKFLOW.md` |
| T3.1 | SRS summary, use-case, architecture, ERD, API list | T2 | Done | `docs/ARCHITECTURE.md` |
| T3.2 | Verify and pin versions from official sources | T2 | Done | `docs/VERSIONS.md` |
| T3.3 | JDK 21 + Maven Wrapper spike | T3.2 | Done | build log |
| T3.4 | Docker / Compose spike | T3.2 | Done | `docker run hello-world` log |
| T3.5 | Jenkins topology spike (container + Docker access) | T3.4 | Done | Jenkins version + `docker ps` from Jenkins |
| T3.6 | Headless Selenium spike | T3.3 | Done | headless Chrome run log |
| T3.7 | Local registry push/pull spike | T3.4 | Blocked (B2) | push + pull log |
| T3.8 | WSL2 Ansible control node + SSH target spike | — | Blocked (B3, B4) | `ansible -m ping` |
| T3.9 | Port, memory, disk inventory | — | Done | environment log |
| T4.1 | Maven skeleton, wrapper, `.gitignore`, `.gitattributes`, `.env.example` | T3.3 | Done | `mvnw verify` log |
| T4.2 | Issue / PR templates, CONTRIBUTING | T2.3 | Done | `.github/` |
| T4.3 | Secret scan and push to origin; create `develop` | T4.1 | Done | scan output, remote refs |
| T5.1 | Issue: delivery creation | T4 | Blocked (B5) | issue URL (B5) |
| T5.2 | Migration V1 + entities + repositories | T4 | Done | migration test |
| T5.3 | Creation service + validation + code generator | T5.2 | Done | unit/integration tests |
| T5.4 | Create form/controller + detail stub | T5.3 | Done | controller tests |
| T5.5 | PR, self-review checklist, merge | T5.4 | Blocked (B5) | PR URL (B5) |
| T6.1 | Security: login/logout, roles, seeded dev accounts | T5 | Done | security tests |
| T6.2 | List/search/filter/pagination | T5 | Done | repository/controller tests |
| T6.3 | Edit permitted fields | T5 | Done | tests |
| T6.4 | Assignment + transition policy + history | T6.1 | Done | transition tests |
| T6.5 | Dashboard aggregation | T6.4 | Done | count tests |
| T6.6 | REST API v1 + health endpoints | T6.4 | Done | API tests |
| T6.7 | UI shell, tokens, responsive pass, admin user list | T6.1 | Done | screenshots |
| T6.8 | Isolated merge-conflict demonstration | T6.2 | Done | conflict transcript |
| T6.9 | Release tag `v0.1.0` | T6.1–T6.8 | Blocked (B5) | tag |
| T7.1 | Jenkins container with persistent volume, plugins | T3.5 | Done | Jenkins console |
| T7.2 | Pipeline job from SCM with polling | T7.1 | Done | poll log |
| T7.3 | Archive JAR + JUnit reports; failing build demo | T7.2 | Done | build logs |
| T8.1 | `Jenkinsfile` stages + parameter | T7 | Done | pipeline log |
| T8.2 | Compose deploy (Nginx → app → PostgreSQL) + health stage | T8.1, T11 | Done | health output |
| T9.1 | Selenium test plan (5 journeys) | T6 | Done | `docs/SELENIUM_TEST_PLAN.md` |
| T9.2 | Selenium suite + screenshot-on-failure + report | T9.1 | Done | report |
| T10.1 | Selenium stage in Jenkins, publish reports always | T8, T9 | Backlog | Jenkins report |
| T10.2 | Deliberate defect → red; fix + regression → green | T10.1 | Backlog | two runs |
| T11.1 | Multi-stage non-root Dockerfile + healthcheck | T6 | Backlog | Dockerfile |
| T11.2 | Container lifecycle transcript | T11.1, B2 | Backlog | transcript |
| T12.1 | Local registry service | T3.7 | Backlog | catalog |
| T12.2 | Pipeline: tag by SHA, push after tests, deploy by tag | T10, T12.1 | Backlog | pipeline log |
| T13.1 | Config spec, inventory, vars, templates | T3.8 | Backlog | playbook |
| T13.2 | Syntax check, check mode, first run | T13.1 | Backlog | run log |
| T14.1 | Second run idempotency | T13.2 | Backlog | run log |
| T14.2 | Health + rollback to previous tag | T14.1, T12 | Backlog | rollback log |
| T15.1 | Full end-to-end run | T1–T14 | Backlog | logs |
| T15.2 | Final docs, troubleshooting, security, limitations | T15.1 | Backlog | docs |
| T15.3 | Evidence INDEX, demo runbook, viva Q&A | T15.2 | Backlog | `docs/evidence/INDEX.md` |

## Milestone plan (no calendar dates — supply them to add a timeline)

| Milestone | Teacher tasks | Exit signal |
|---|---|---|
| M1 Planning baseline | T1–T2 | Docs merged to `main` |
| M2 Foundation | T3–T4 | Versions pinned, skeleton builds, repo pushed |
| M3 MVP | T5–T6 | A1–A7 pass, tag `v0.1.0` |
| M4 Continuous integration and testing | T7–T10 | Jenkins green; red-then-green gate evidence |
| M5 Containers and delivery | T11–T12 | Image in local registry deployed by tag |
| M6 Operations and release | T13–T15 | Idempotency, rollback, final evidence index |

Dates were not supplied, so none are invented. The user may add sprint dates per milestone.

## Definition of Done

A ticket is **Done** only when all apply:

1. Acceptance criteria met, with the evidence listed above actually captured.
2. Server-side validation and authorization included where relevant; no dead controls.
3. Focused tests written and passing; regression test added for any fixed defect.
4. `mvnw verify` passes (for code changes); failures are fixed, never skipped or weakened.
5. Secret scan of the diff is clean; no generated output or local config committed.
6. Docs, diagrams and examples updated to match behaviour.
7. Diff self-reviewed using the PR checklist in `docs/GIT_WORKFLOW.md`; merged via the branch policy.
8. `docs/IMPLEMENTATION_LOG.md` updated with commands, results and known issues.

Blocked work stays in **Blocked** with its blocker ID; it is never moved to Done on the strength of files existing.
