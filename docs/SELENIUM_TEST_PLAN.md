# Selenium Test Plan (Teacher Task 9)

## Scope

Five critical browser journeys from `docs/App_Flow.md` §8, run against the real application
(random port, embedded PostgreSQL 17, `test` profile) in headless Chrome.

| Journey | Class | Assertions (observable outcomes, not just navigation) |
|---|---|---|
| J1 Create delivery | `J1CreateDeliverySeleniumIT` | Success flash; code matches `LMD-\d{8}-\d{6}`; badge `CREATED`; 1 history entry; 1 DB row. Invalid input: error summary, stays on form, typed address preserved, phone marked invalid, 0 rows. Detail shows `+91 98765 43210`. Another country: India (+91) is the default, choosing United Kingdom changes the shown code to +44, `07400123456` is saved and shown as `+44 7400 123456`, the edit form recovers United Kingdom and `7400123456`. A 12-digit Indian number with 91 typed in is refused with the country-code message. |
| J2 Search and open | `J2SearchDeliverySeleniumIT` | Result count 3 → 1 after search by reference; the single row has the expected code; detail shows that reference and recipient. No-match search shows empty state; Reset restores the list. Status + agent filters: Created + Arjun Agent gives the empty state, Assigned + Arjun Agent gives exactly that row with the applied filters stated; after the browser's Back button the unfiltered page shows All/All, never the previously chosen filters (regression for the Stage C report). |
| J3 Assign and visibility | `J3AssignmentVisibilitySeleniumIT` | Dispatcher assigns → flash, badge `ASSIGNED`, agent shown. Assigned agent's list contains exactly that delivery. Other agent's list is empty and the direct URL gives the 404 page without the recipient's name. |
| J4 Agent progression | `J4AgentProgressSeleniumIT` | Dashboard before: assigned 1, delivered 0. Out for delivery → Delivered with flashes and badges; 4 history entries; no actions panel; DB status `DELIVERED`; dashboard after: delivered 1, assigned 0. |
| J5 Rejected actions | `J5RejectedActionsSeleniumIT` | Failing without a reason shows the error, badge and DB stay `OUT_FOR_DELIVERY`, history count unchanged; with a reason it becomes `FAILED`. Agent has no create link and the direct URL gives the 403 page without the form. |

## Design rules

- **Locators:** `data-testid` attributes only (no CSS class chains, no translated text lookups).
- **Synchronisation:** explicit waits only; no `Thread.sleep`. `click()` scrolls instantly (Bootstrap's
  smooth scrolling otherwise races WebDriver) and waits for a new document using a `window` marker.
- **Data:** each test truncates the tables and creates its own fictional users/deliveries through the
  real service (`TestData`), so tests are independent and repeatable.
- **Failure evidence:** `FailureArtifactsExtension` saves `<test>.png`, `<test>.html` and `<test>.txt`
  (URL, title, error) to `target/selenium-artifacts/<Class>/` before the browser closes.
- **Reports:** JUnit XML in `target/failsafe-reports/` (for Jenkins) and HTML in
  `target/reports/failsafe.html`, generated in `post-integration-test` so it exists even when a
  journey fails.

## Commands

```powershell
. .\scripts\use-jdk21.ps1
.\mvnw.cmd -B verify -Pselenium                          # all tests + journeys
.\mvnw.cmd -B verify -Pselenium -Dtest=NoSuchTest -Dsurefire.failIfNoSpecifiedTests=false   # journeys only
.\mvnw.cmd -B verify -Pselenium -Dselenium.headless=false  # watch the browser
```

CI/container options: `-Dselenium.chrome.binary=/usr/bin/chromium -Dselenium.noSandbox=true`.
The default `mvnw verify` (no profile) does not run browser journeys.

## Environment

Chrome 155.0.8059.81 (Windows), Selenium 4.43.0 (Spring Boot-managed), driver resolved by
Selenium Manager (chromedriver 155.0.8059.84 observed in capabilities).
