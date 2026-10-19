# Phase 10 — Continuous Testing in Jenkins (2026-10-19)

The `Selenium quality gate` stage runs the five journeys (8 tests) in the Jenkins container's
Chromium 155 with Debian's chromedriver, after unit/integration tests and **before** image packaging
and deployment. JUnit results, the HTML report (`Selenium report` link) and failure artifacts are
published even when the stage fails.

| Build | Commit | What | Unit/integration | Selenium | Package / Deploy | Result |
|---|---|---|---|---|---|---|
| #8 | `62d40a36b414` | gate added | 86/86 | **8/8** | built; deployed, revision verified | SUCCESS |
| #9 | `7f3a6e401a85` | **deliberate defect**: failure textarea renamed `failureReason` | 86/86 (cannot see it) | **7/8 — J5 failed** | **skipped due to earlier failure**; no image built for the commit | FAILURE |
| #10 | `eb87044cb837` | fix + MockMvc regression test | 87/87 | 8/8 | built; deployed `eb87044cb837…` | SUCCESS (95 tests in Jenkins report) |

Deployment unchanged by the red build: `lastmile-app-1` revision `62d40a36b414…` with the same
`StartedAt` (`2026-10-18T22:27:14Z`) before and after build #9; `docker images lastmile-delivery`
had no `7f3a6e401a85` tag.

Failure evidence archived by Jenkins (build #9): `jenkins-build-9-J5-failure.png` — the agent
entered a reason, but the server replied "A reason is required when a delivery fails." and the
delivery stayed *Out for delivery*.

Regression test `WebAuthorizationTest.failureFormSubmitsTheReasonUnderTheParameterTheControllerReads`:
failed against the defect (`Tests run: 11, Failures: 1`), passes with the fix.

The defect commit stays in branch history, clearly labelled, followed immediately by its fix;
the default branch was never broken.

Logs: `build-8-selenium-green.log`, `build-9-defect-red.log`, `build-10-fixed-green.log`.
