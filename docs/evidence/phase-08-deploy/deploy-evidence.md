# Phase 8 — Pipeline as Code and Deployment Evidence (2026-10-19)

Pipeline: `Jenkinsfile` on `ci/jenkins-pipeline` (stages: Checkout → Validate parameters → Build and
unit/integration tests → Package images → Deploy → Verify deployment; post: JUnit, archive, diagnostics).

## Successful SCM-triggered deployment — build #5 (`build-5-deploy.log`)

Cause `Branch indexing`; commit `3af9df95d0b1`; parameters `DEPLOY_PROFILE=demo, HTTP_PORT=8088, DEPLOY=true`.

```text
[INFO] Tests run: 86, Failures: 0, Errors: 0, Skipped: 0
app image sha256:898152c9f82d… user=10001:10001 revision=3af9df95d0b1227fc7025b2d673653cb3edadf43
 Container lastmile-db-1 Healthy / lastmile-app-1 Healthy / lastmile-nginx-1 Healthy
SERVICE   IMAGE                            STATUS
app       lastmile-delivery:3af9df95d0b1   Up 26 seconds (healthy)
db        postgres:17.11-alpine3.24        Up 38 seconds (healthy)
nginx     lastmile-nginx:3af9df95d0b1      Up 14 seconds (healthy)
readiness via nginx: {"status":"UP"}
deployed app revision: 3af9df95d0b1227fc7025b2d673653cb3edadf43
Finished: SUCCESS
```

## Checks from the Windows host

| Check | Result |
|---|---|
| `GET http://127.0.0.1:8088/api/v1/health/ready` | `{"status":"UP"}` HTTP 200 |
| `GET http://127.0.0.1:8088/login` | HTTP 200 |
| `GET /` unauthenticated | 302 → `http://127.0.0.1:8088/login` (port preserved through Nginx) |
| Published ports | only `lastmile-nginx-1 127.0.0.1:8088->80/tcp`; app `8080/tcp` and db `5432/tcp` not published |
| Secrets in console logs | none of the three secrets appear in any saved log (checked) |
| UI | `deployed-login.png`, `deployed-dashboard.png` (dispatcher, 6 seeded fictional deliveries) |

## Parameter evidence

| Build | Trigger | Parameters | Result |
|---|---|---|---|
| #6 (`build-6-invalid-parameter.log`) | `Started by user admin` (API) | `HTTP_PORT=80` | **FAILURE** at *Validate parameters*: `HTTP_PORT must be a number between 1024 and 65535, got '80'`; build, package, deploy and verify **skipped due to earlier failure**; running app unchanged (same revision and `StartedAt` before/after) |
| #7 (`build-7-http-port-8089.log`) | `Started by user admin` (API) | `HTTP_PORT=8089` | **SUCCESS**; Nginx re-published on `127.0.0.1:8089`; readiness 200 on 8089; 8088 closed |

## Deployed URL

`http://127.0.0.1:8088` — a **local** URL on the workstation (default parameter). No public URL exists
or is claimed.
