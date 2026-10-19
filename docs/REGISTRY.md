# Local Docker Registry and Continuous Deployment (Teacher Task 12)

## Registry

`infra/registry/compose.yaml` runs `registry:3.1.2` (container `lastmile-registry`, volume
`registry_data`) published on **127.0.0.1:5000 only**. It has no authentication, so it must never
be bound to a LAN or public interface.

```powershell
docker compose -f infra/registry/compose.yaml up -d
curl.exe http://127.0.0.1:5000/v2/_catalog
curl.exe http://127.0.0.1:5000/v2/lastmile-delivery/tags/list
```

## One registry, several names (risk R2)

| Who | Address used | Why |
|---|---|---|
| Docker daemon (push, `compose pull`) | `localhost:5000` | Port published on the Docker host loopback; Docker trusts `localhost` registries without TLS |
| Jenkins container (registry API checks) | `lastmile-registry:5000` | `localhost` inside the container is Jenkins itself; both containers share the `lastmile-registry` network |
| Isolated Ubuntu target (Phases 13–14) | host-only address of the workstation, set as `lastmile_registry` in the inventory | Depends on BLOCKERS B4; listed in the target daemon's `insecure-registries` |

Because names differ per namespace, identity is proven by **digest**: the pipeline compares the
digest returned by `docker push` with the registry's `Docker-Content-Digest` header for the same tag.

## Pipeline flow

1. Unit/integration tests and the Selenium gate must pass.
2. `docker build` tags `localhost:5000/lastmile-delivery:<sha12>` and `localhost:5000/lastmile-nginx:<sha12>`.
3. `docker push`, then the digest check above (fails the build on mismatch).
4. **Deploy**: records the running release (last known good), `docker compose pull` of the exact
   tags from the registry, `up --wait`, readiness via Nginx and revision == commit.
5. If step 4 fails, the recorded release is redeployed and verified (**automatic rollback**), and the
   build still fails so the bad candidate is visible.

Tags are immutable commit SHAs; `latest` is never used. Older tags stay in the registry, so any
previous release can be redeployed by tag.

## Recovery drill

*Build with Parameters* → `FAULT_INJECTION=unreachable-database` deploys the candidate with
`LASTMILE_DB_HOST=db-does-not-exist`; its health check fails and the pipeline must roll back.
Evidence: `docs/evidence/phase-12-registry/`.
