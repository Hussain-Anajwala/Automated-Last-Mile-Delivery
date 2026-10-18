# Phase 12 — Jenkins → Local Registry → Deployment (2026-10-19)

Registry `registry:3.1.2` at `127.0.0.1:5000` (loopback only); Jenkins reaches its API as
`lastmile-registry:5000` on the shared Docker network (verified `GET /v2/` → `{}` HTTP 200).
Current catalog and tags: `registry-state.txt`.

## Build #13 — publish and deploy by registry reference (`build-13-publish-deploy.log`)

Commit `e13d79d75af7` (cause: branch indexing). Unit/integration 87/87, Selenium 8/8, then:

```text
lastmile-delivery:e13d79d75af7 pushed digest=sha256:d9a6bcec…f247 registry digest=sha256:d9a6bcec…f247
lastmile-nginx:e13d79d75af7    pushed digest=sha256:717e5c83…1efc registry digest=sha256:717e5c83…1efc
Last known good before deploy: app=lastmile-delivery:887f5e074412 (local tag from before the registry)
 Image localhost:5000/lastmile-nginx:e13d79d75af7 Pulled
 Image localhost:5000/lastmile-delivery:e13d79d75af7 Pulled
readiness via nginx: {"status":"UP"}
deployed app revision: e13d79d75af7165526dcbc2c9cc4d3bbe1cd314d
Finished: SUCCESS
```

## Build #15 — recovery drill with automatic rollback (`build-15-rollback-drill.log`)

Triggered with `FAULT_INJECTION=unreachable-database` for commit `881a67f31d06`.

```text
[INFO] Tests run: 87 … / [INFO] Tests run: 8 …                       (gates passed)
lastmile-delivery:881a67f31d06 pushed digest=sha256:2e9d7596…58c6 registry digest=sha256:2e9d7596…58c6
Last known good before deploy: app=localhost:5000/lastmile-delivery:e13d79d75af7 … revision=e13d79d75af7…
FAULT INJECTION: candidate deployed with LASTMILE_DB_HOST=db-does-not-exist (recovery drill)
dependency failed to start: container lastmile-app-1 is unhealthy
ROLLBACK: restoring localhost:5000/lastmile-delivery:e13d79d75af7 / localhost:5000/lastmile-nginx:e13d79d75af7
readiness via nginx: {"status":"UP"}
deployed app revision: e13d79d75af7165526dcbc2c9cc4d3bbe1cd314d
ROLLBACK SUCCEEDED: localhost:5000/lastmile-delivery:e13d79d75af7 is healthy again
Finished: FAILURE      (deliberately: the candidate failed)
```

After the drill, from the Windows host: `lastmile-app-1` = `localhost:5000/lastmile-delivery:e13d79d75af7`,
health `healthy`; `GET http://127.0.0.1:8088/api/v1/health/ready` → `{"status":"UP"}` HTTP 200.

## Notes

- Build #12 (`build-12-interrupted-by-jenkins-restart.log`) ended with `script returned exit code -1`
  because the Jenkins container was recreated (to join the registry network) while it was running.
  Not a code failure.
- Build #14 was aborted on purpose so the drill would roll back to a *different* image.
- Pull by the **isolated Ubuntu target** is not yet shown; it depends on BLOCKERS B4 and is part of
  Phase 14. The registry is never exposed beyond loopback.
