# Stage C — delivery filters, phone validation, Jenkins audit (2026-10-23)

Branch `fix/delivery-filters-phone-country-code` (draft PR #21). Nothing here was merged or deployed; `main`,
`v1.0.0` and both running deployments (workstation 8088, WSL2 target 8089) are unchanged.

## A. Delivery filters

| Step | Evidence |
|---|---|
| Server behaviour of the released code over real HTTP: `status=CREATED&agentId=<Arjun>` → 0 rows + empty state; `ASSIGNED`+Arjun → exactly his row; options rendered `selected`; `status=LOST`, `agentId=abc` → 400 | `filter-repro-v1.0.0-http.txt` |
| Browser reproduction on v1.0.0: filter, Search, Back → unfiltered rows under a Status control still showing "Created" | `filter-j2-on-v1.0.0-fails.txt` (expected "All" but was "Created") |
| Fix verified: the same journey passes; rendered-HTML tests for AND filters, text search, pagination, agent scope, malformed parameters | `test-results-17d8640.txt` (J2 3/3, DeliveryListWebTest 8/8) |

**Root cause:** not the query, binding or controller. The browser restored the user's last dropdown choices onto
a page that the server had rendered without those filters (back/forward navigation). Fix: `autocomplete="off"`
on the search form, a `pageshow` reset for pages restored from the back/forward cache, and an "applied filters"
line rendered from the server-side criteria. Chrome was the only browser automated; Firefox and Safari were not tested.

## B. Phone numbers

Rule and storage: `docs/Backend_Schema.md` §6.1, decision D21. Tests: `PhoneNumbersTest` 40, `DeliveryPhoneTest` 7,
`DeliveryCreationControllerTest` 10, `DeliveryApiTest` 9, J1 journeys 4 (country selection, display, edit recovery).

## C. Jenkins audit (read-only JSON API, no builds triggered)

| Branch / build | State | Cause | Verdict |
|---|---|---|---|
| `ci/demo-failing-build` #1 | red | one deliberate assertion in `DeliveryCodeGeneratorTest` (86 tests, 1 failure) at "Build and unit/integration tests"; later stages not run | expected demonstration |
| `ci/jenkins-pipeline` #15 | failed, superseded by #16 | `FAULT_INJECTION=unreachable-database`; "ROLLBACK SUCCEEDED" | intentional recovery drill |
| `main` #2, `infra/ansible-wsl-target` #2 | failed, superseded | checkout: "Could not resolve host: github.com" | transient network |
| `docs/release-v1.0.0-evidence` #1 | failed, superseded by #2 | Jenkins internal error in Checkout: "No build record … could be located" | transient Jenkins error; cause not determined |
| all other branches incl. `main` #3, `develop` #6, PR #21 #2 | green | non-`main` builds log `Stage "Deploy" skipped due to when conditional` | as designed |

Queue empty, nothing building. No Jenkins configuration change was needed.
