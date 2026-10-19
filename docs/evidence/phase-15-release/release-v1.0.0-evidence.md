# Release v1.0.0 — end-to-end evidence (2026-10-19)

`v1.0.0` → commit `ff1cac19ee86cfde8ca09b6019222fbd6abdb674` (merge of release PR #18). The tag was created
only after every step below had passed. `main` was left at this commit, so the deployed workstation
release equals the tag; this evidence was added afterwards on `develop`.

| Step | Result | Evidence |
|---|---|---|
| Source | Release PR #18 (`develop` → `main`); `main` tree = tree of `6ea11637`, which Jenkins had tested (docs/phase-15-release #4: 89 + 8) | PR #18 |
| Secret gate | full-history scan passed before the tag push (`scripts/safe-push.sh`) | session log |
| Jenkins `main` #2 | **FAILURE before checkout**: `Could not resolve host: github.com` inside the Jenkins container (transient DNS; `git ls-remote` worked minutes later). Not a code or test failure. | `main-2-dns-failure.log` |
| Jenkins `main` #3 (re-run, default parameters) | 89 unit/integration + 8 Selenium tests passed; app `sha256:7f1051da382c0faee86f200a1a6cd555efaf003fdf94c9e62838ed7028a265e3`, nginx `sha256:3e47ae1772dabc0e75b0308ae5844329d0188de8784e8c17cac6c6c5fbfd0f85` (pushed = registry); last known good recorded (`e3b9d31cae5b`); deployed; readiness UP; revision = commit | `main-3-release-v1.0.0.log` |
| Workstation | `localhost:5000/lastmile-delivery:ff1cac19ee86`, healthy, `GET http://127.0.0.1:8088/api/v1/health/ready` → 200; repo digest `sha256:7f1051da…`; database still holds the 6 deliveries / 15 history rows created 2026-10-18 22:15 UTC (DB container never recreated) | `workstation-state.txt` |
| Ansible target: upgrade | `site.yml -e lastmile_image_tag=ff1cac19ee86` → `ok=25 changed=4 failed=0`; target digests **identical** to Jenkins' (`7f1051da…`, `3e47ae17…`); readiness 200 | `t1-upgrade-to-release.log`, `target-state.txt` |
| Ansible target: idempotency | same command again → **`ok=24 changed=0 failed=0`**; app container start time unchanged | `t2-idempotency-release.log` |
| Ansible target: recovery | `rollback.yml` → `e3b9d31cae5b` (v0.1.0) healthy, 200 (`changed=4`); roll-forward → `ff1cac19ee86` healthy, 200 (`changed=4`) | `t3-…`, `t4-…` |
| Data preservation (target) | 6 deliveries / 15 history rows created 2026-10-19 02:33:56 UTC present after upgrade, second run, rollback and roll-forward; DB container not recreated | `target-state.txt` |

Note: the first snapshot in `target-state.txt` was taken seconds after the target distribution was resumed,
while its stack was still starting (`health=starting`, DB not yet accepting connections). By the upgrade the
stack was healthy.

Not claimed: Jenkins does not trigger Ansible (the target step is a manual control-node run); the target is
a WSL2 distribution, not a separate VM (D20); there is no public URL.
