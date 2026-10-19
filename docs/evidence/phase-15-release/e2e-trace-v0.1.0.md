# End-to-end trace of release v0.1.0 (2026-10-19)

One commit followed from Git to both deployment targets, using only recorded evidence.

| Step | What happened | Evidence |
|---|---|---|
| 1. Source | Tag `v0.1.0` → commit `e3b9d31cae5bd48eb1bfbaf3db56e21f0ddc56e3` on `main` (merge of release PR #15; tree identical to the locally tested merge `c2f4ff68`) | `git show v0.1.0`, `phase-06-mvp/pull-requests-and-release.md` |
| 2. CI gates | Jenkins `main` #1: 89 unit/integration + 8 Selenium tests passed | `phase-12-registry/main-1-release-deploy.log` |
| 3. Images | `localhost:5000/lastmile-delivery:e3b9d31cae5b` digest `sha256:64eb5a335df32135b23a4a12e21521851aad276b2ccf7aa7dea550731f1a0466`; `lastmile-nginx:e3b9d31cae5b` digest `sha256:f88d4fb53f4d9cb49e0a170835b859f0ca6500f6ae296794a9d31988a4c3810e`; pushed digest = registry digest | same log |
| 4. Workstation deploy | Jenkins deployed it behind Nginx at `http://127.0.0.1:8088`; readiness UP; revision label = commit | same log |
| 5. Ansible target | `site.yml -e lastmile_image_tag=e3b9d31cae5b` on `lastmile-target`: `ok=25 changed=4 failed=0`; the target's images have **the same digests** as step 3; revision label = commit; readiness via the target's Nginx `{"status":"UP"}` HTTP 200 | `phase-13-ansible/09-e2e-v0.1.0-to-target.log`, this file |

Observed on the way: after the target distribution was restarted, its stack came back automatically on the
last recorded release (`restart: unless-stopped`) before the upgrade.

Not claimed: Jenkins does not invoke Ansible — the target deployment is a separate, manual control-node
step (allowed by App_Flow §10 "as designed"). The target's acceptability as a WSL2 "clean node" awaits the
teacher (B6 Q3).

Target digest check (2026-10-19):

```text
localhost:5000/lastmile-nginx:e3b9d31cae5b sha256:f88d4fb53f4d9cb49e0a170835b859f0ca6500f6ae296794a9d31988a4c3810e
localhost:5000/lastmile-delivery:e3b9d31cae5b sha256:64eb5a335df32135b23a4a12e21521851aad276b2ccf7aa7dea550731f1a0466
records: current=e3b9d31cae5b previous=0ac1a53c836f
app image localhost:5000/lastmile-delivery:e3b9d31cae5b revision=e3b9d31cae5bd48eb1bfbaf3db56e21f0ddc56e3 health=healthy
readiness via nginx: {"status":"UP"} HTTP 200
```
