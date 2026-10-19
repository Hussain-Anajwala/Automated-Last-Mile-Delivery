# Phase 6 — MVP Acceptance Evidence (2026-10-19)

Branch `feature/delivery-workflow` (stacked on `feature/delivery-create`, decision D13).
Suite: `mvnw.cmd -B verify` → **Tests run: 86, Failures: 0, Errors: 0, Skipped: 0**, BUILD SUCCESS, exit 0.

| ID | Criterion (`docs/PROBLEM_SCOPE.md` §7) | Automated evidence | Live evidence |
|---|---|---|---|
| A1 | Create with generated code, CREATED, initial history | `DeliveryCreationServiceTest`, `DeliveryCreationControllerTest`, `DeliveryApiTest.dispatcherCreates…` | API smoke: `201 status=CREATED code=LMD-20261018-000007` |
| A2 | Invalid input rejected, nothing saved | `DeliveryCreationControllerTest.invalidSubmission…`, `blankPhoneShowsExactlyOneMessage`, `DeliveryApiTest.validationErrorsAre400…` | Phase 5 smoke; `screenshots/form-errors-*.png` |
| A3 | Search by code/name/reference; filters; empty state | `DeliverySearchServiceTest` (5 tests incl. LIKE escaping, agent scoping, pagination), `WebAuthorizationTest.searchWithNoMatches…` | API smoke: `q=ord-200&status=CREATED → 2 ['ORD-2002','ORD-2001']`; `screenshots/list-*.png` |
| A4 | Agents cannot reach others' deliveries or staff actions | `WebAuthorizationTest` (crafted agentId, 404 detail/status, 403 edit/assign/admin), `DeliveryApiTest.agentCannot…`, `…EnforcesOwnership…`, `browserSessionCookiesAreNotAcceptedByTheApi` | API smoke: agent.two → `HTTP 404`; agent.one unassigned → `HTTP 404` |
| A5 | Disallowed transitions rejected, nothing changes | `DeliveryStatusTest` (all 25 pairs), `DeliveryPolicyTest`, `DeliveryWorkflowServiceTest.invalidTransitions…`, `failingRequiresAReason…`, `staleVersion…` | API smoke: `HTTP 409` CREATED→DELIVERED; `HTTP 400` FAILED without reason; `HTTP 409` edit delivered |
| A6 | One history row per transition, actor from session, atomic | `DeliveryWorkflowServiceTest.agentCompletesTheHappyPath…`, `statusAndHistoryAreAtomicWhenTheHistoryInsertFails` (history insert forced to fail → status unchanged) | API smoke: history `['CREATED','ASSIGNED','OUT_FOR_DELIVERY','DELIVERED']`; `screenshots/detail-*.png` |
| A7 | Dashboard counts equal DB counts in scope | `DashboardServiceTest` (compared with direct `GROUP BY` SQL for staff and agent; updates after change) | API smoke: 6 seeded → 7 after create; `screenshots/dashboard-*.png` |

## UI review

Screenshots of the running app (headless Chrome 155 via Selenium 4.43, 1366 px and 390 px) are in
`screenshots/`. The review found and fixed two defects (commit `8226f95`): duplicate phone error
messages, and delivery codes wrapping at 390 px. A harness issue (Bootstrap's smooth scrolling
racing Selenium's click) was handled in the capture script, not the product.

## Git collaboration

- Second feature branch: `feature/delivery-workflow`.
- Merge conflict created and resolved on disposable branches: `merge-conflict-transcript.md`
  (`demo/conflict-base` resolution commit `cbe5031`, pushed).
- Release tag `v0.1.0`: **BLOCKED** — tags go on `main` (docs/GIT_WORKFLOW.md), and `main` can
  only receive these features through PRs, which need `gh` authentication (BLOCKERS B5).
