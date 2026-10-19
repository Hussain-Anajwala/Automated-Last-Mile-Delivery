# Phase 9 — Selenium Evidence (2026-10-19)

Branch `test/selenium-journeys` (stacked on `feature/delivery-workflow`). Windows 11, Chrome
155.0.8059.81 headless, Selenium 4.43.0, JDK 21.0.12.1.

## Runs (exact results)

| Run | Command | Result |
|---|---|---|
| First full run | `mvnw.cmd -B verify -Pselenium` | exit 0 — unit/integration `Tests run: 86, Failures: 0, Errors: 0, Skipped: 0`; journeys `Tests run: 8, Failures: 0, Errors: 0, Skipped: 0` |
| Repeat 2 | journeys only | exit 0 — 8/8 |
| Repeat 3 | journeys only | **exit 1 — `Errors: 1`** in J4 (see below) |
| After harness fix, 5 consecutive runs | journeys only | exit 0 ×5 — 8/8 each, no failure artifacts |
| Deliberate defect | journeys only, reason check removed from `DeliveryService` in the working copy | **exit 1** — J5 `failureWithoutAReasonIsRejectedAndNothingChanges` failed; artifacts saved |
| After reverting the defect | `mvnw.cmd -B clean verify -Pselenium` | exit 0 — 86/86 and 8/8 |

## Flaky run diagnosed (repeat 3)

`flaky-run-diagnosed/`: Chrome answered `isElementEnabled` on the previous page's `<html>` node
with `unknown error: unhandled inspector error … Node with given id does not belong to the
document` during the login redirect, which `stalenessOf` does not treat as stale. The harness now
detects the new document with a `window` marker instead of polling old DOM nodes. Five
consecutive green runs followed. This was a test-harness defect; product behaviour was correct
(the screenshot shows the dashboard loaded normally).

## Known defect caught (failure screenshot mechanism)

`defect-caught/`: with the "failure requires a reason" rule removed (never committed), J5 timed
out waiting for the error flash; the screenshot shows the delivery wrongly moved to **Failed** with
"(no reason given)". The working copy was restored with `git checkout -- DeliveryService.java`
(diff confirmed empty) before the final green run.

## Reports

`passing-report/failsafe.html` — HTML report of the final passing run. JUnit XML is produced in
`target/failsafe-reports/` on every run (for Jenkins in Phase 10).
