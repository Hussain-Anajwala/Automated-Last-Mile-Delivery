# Phase 7 — Jenkins CI Evidence (2026-10-19)

Jenkins `2.580.1` (header `X-Jenkins: 2.580.1`), image `lastmile-jenkins:2.580.1` built from
`ci/jenkins/Dockerfile`, running at `http://127.0.0.1:8090` (localhost only).

## Setup checks

| Check | Result |
|---|---|
| Image build | 2 attempts failed because the WSL VM restarted (Docker Desktop update to 29.8.2, then commit-limit exhaustion — BLOCKERS B7); 3rd attempt `Image lastmile-jenkins:2.580.1 Built` |
| Start | `Jenkins is fully up and running`; login page HTTP 200 after ~12 s; no setup wizard (JCasC) |
| Security | anonymous `GET /api/json` → **403**; admin → 200 |
| Tools in container | `openjdk 21.0.12.1 LTS`; `docker cli 29.9.1 -> server 29.8.2`; `Docker Compose v5.6.0`; `Chromium 155.0.8059.79`; `ChromeDriver 155.0.8059.79`; user `jenkins` with group 0 (socket access) |
| Plugins | `installed-plugins.txt` (77); top-level versions equal the pins |

## Trigger, builds and artifact

| Build | Cause | Commit | Result | Tests | Artifact |
|---|---|---|---|---|---|
| `ci/jenkins-pipeline` #1 | Branch indexing (periodic scan found the new branch's `Jenkinsfile` at 21:56:10 UTC) | `d21fe6c28e4d` | **SUCCESS** (147 s) | 86 passed / 0 failed | `target/lastmile-delivery.jar` |
| `ci/demo-failing-build` #1 | Branch indexing | `dd773f616ff2` (deliberately wrong assertion) | **FAILURE** — `Tests run: 86, Failures: 1`, `BUILD FAILURE`, `script returned exit code 1` | 85 passed / 1 failed | none |
| `ci/jenkins-pipeline` #2 | Branch indexing (new commit pushed 22:00:02 UTC) | `99f1369e80db` | **SUCCESS** (88 s) | 86 passed / 0 failed | `target/lastmile-delivery.jar` |

Logs: `build-ci-jenkins-pipeline-1.log`, `build-ci-jenkins-pipeline-2.log`,
`build-ci-demo-failing-build-1.log`, `branch-indexing.log`. No credentials appear in them (checked).
The `ci/demo-failing-build` branch is intentionally red and must never be merged.
