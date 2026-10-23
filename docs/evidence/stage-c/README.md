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

## D. Owner-reported manual Chrome verification

The project owner reports that all nine checks below passed in Chrome against the isolated preview at
`http://127.0.0.1:8091`. The preview app image at the time was
`localhost:5000/lastmile-delivery:c98c2ead24ea`, built from full source SHA
`c98c2ead24ead580a3f7fc2690a11e275f3d8abc`.

| # | Owner-reported check | Result |
|---|---|---|
| 1 | Combined status and agent filters | Passed |
| 2 | Text search and combined filters | Passed |
| 3 | Empty state and Reset | Passed |
| 4 | Pagination preserves filters | Passed |
| 5 | Browser Back/Forward keeps controls, rows, and applied-filter summary consistent | Passed |
| 6 | Indian phone-number validation | Passed |
| 7 | International phone-number validation | Passed |
| 8 | Editing recovers the saved country and national number | Passed |
| 9 | Cancelling an edit preserves the stored phone number | Passed |

These are results reported by the project owner; Codex did not perform or observe these manual browser
checks and no screenshots, browser-console logs, or HTTP traces were supplied. The manual checks apply to
the `c98c2ead` application source only. Documentation synchronization with `develop` does not change
application source; a refreshed preview will use the final merged commit's candidate image, but no new
human browser run is claimed for that image.

## E. Jenkins feature build #3 on the tested candidate

Verified from the local Jenkins API and build console on 2026-10-23: build
[`fix/delivery-filters-phone-country-code` #3](http://localhost:8090/job/lastmile-delivery/job/fix%252Fdelivery-filters-phone-country-code/3/)
checked out full SHA `c98c2ead24ead580a3f7fc2690a11e275f3d8abc` and completed **SUCCESS**. Maven reported
147 unit/integration tests and 11 Selenium tests, with 0 failures, 0 errors, and 0 skips in each suite.
The `lastmile-delivery:c98c2ead24ea` image digest was
`sha256:39354f7dd90bf7e3287dd8eec6c4f73d74f13b0536861a1292f418b8befbb22d`; the
`lastmile-nginx:c98c2ead24ea` digest was
`sha256:630f57bd6f5fd8195865952842aeeb41502e0100a86039bca52b785aee7fe84c`. Jenkins verified each
registry digest matched its locally built image. The feature-branch `Deploy` stage was skipped as intended.
This is historical automated evidence for `c98c2ead`; it is not a Jenkins result for the later merged
commit.
