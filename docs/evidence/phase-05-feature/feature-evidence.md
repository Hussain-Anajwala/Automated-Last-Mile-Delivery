# Phase 5 — Delivery Creation Feature Evidence (2026-10-19)

Branch: `feature/delivery-create` (from `develop` 13af8d2).

## Automated tests — `mvnw.cmd -B verify` (JDK 21.0.12.1), exit 0

```
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.286 s -- in com.lastmile.delivery.DeliveryCodeGeneratorTest
Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 22.95 s -- in com.lastmile.delivery.DeliveryCreationControllerTest
Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.738 s -- in com.lastmile.delivery.DeliveryCreationServiceTest
Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.072 s -- in com.lastmile.delivery.DeliveryStatusTest
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.102 s -- in com.lastmile.health.HealthControllerTest
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.506 s -- in com.lastmile.health.HealthControllerUnitTest
Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.917 s -- in com.lastmile.SchemaConstraintsTest
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.857 s -- in com.lastmile.security.LoginTest
Tests run: 35, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

The first run had 1 failure: `unauthenticatedUserIsRedirectedToLogin` expected the pattern `**/login`, but Spring Security 7 redirects to the relative `/login`. Behaviour was correct; the assertion was tightened to an exact `redirectedUrl("/login")` and the suite re-run (35/35).

## Live application run (no Docker; embedded PostgreSQL 17 via `DevApplication`)

```
mvnw.cmd spring-boot:test-run -Dspring-boot.run.main-class=com.lastmile.DevApplication   (port 18080, demo profile)
Successfully applied 1 migration to schema "public", now at version v1 (execution time 00:00.040s)
Demo accounts ensured: 4 created, 0 already present
GET /api/v1/health/ready -> 200 {"status":"UP"}
```

HTTP smoke test (`scripts/smoke_create.py`; demo password read from an untracked local file):

```
login dispatcher@lastmile.test: 302 -> http://localhost:18080/
valid create: 302 -> http://localhost:18080/deliveries/2
detail: 200 code=LMD-20261018-000002 status=CREATED flash=True history_rows=1
invalid create: 200 location=None error_summary=True phone_msg=True input_preserved=True
login agent.one@lastmile.test: 302 -> http://localhost:18080/
agent GET /deliveries/new: 403
agent GET unassigned /deliveries/1: 404
wrong password: 302 -> http://localhost:18080/login?error
```

The valid POST also carried `status=DELIVERED` and `deliveryCode=HACK`; the stored record shows status `CREATED` and a generated `LMD-` code. Codes use the UTC date (`20261018` at 02:20 IST, which is 20:50 UTC on 18 Oct).

The harness's first attempt failed for a harness reason (password file written with CRLF on Windows); the file was regenerated without line endings before the recorded run. That attempt had already created delivery 1, which is why the recorded run created delivery 2.

Screenshots: `login-desktop.png` (1280 px), `login-mobile.png` (390 px).
