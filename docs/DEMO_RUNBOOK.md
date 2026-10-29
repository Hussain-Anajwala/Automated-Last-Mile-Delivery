# Demonstration Runbook

A sequence for a live demo or video, about 15–20 minutes. Everything runs locally; there is no
public URL. Close heavy applications first (memory, BLOCKERS B7) and run one heavy job at a time.

## 0. Start services (PowerShell, repository root)

```powershell
docker compose -f infra/registry/compose.yaml up -d
docker compose -f ci/jenkins/compose.yaml up -d
docker compose -p lastmile ps          # the app stack restarts automatically (restart: unless-stopped)
curl.exe http://127.0.0.1:8088/api/v1/health/ready
```

Jenkins: http://127.0.0.1:8090 (user `admin`, password in the git-ignored `ci/jenkins/.env` — do not show it on screen).

## 1. Product (5 min) — http://127.0.0.1:8088

Demo accounts are fictional (`dispatcher@lastmile.test`, `agent.one@lastmile.test`, `admin@lastmile.test`);
their password is `LASTMILE_DEMO_PASSWORD` in `ci/jenkins/.env`.

1. Dispatcher: dashboard counts → create a delivery (show validation errors first) → generated code.
2. Search by reference → detail → assign to *Arjun Agent* → history row appears.
3. Agent: sees only own deliveries; open another agent's delivery URL → 404 page.
4. Agent: Out for delivery → try *Mark as failed* without a reason (rejected) → Delivered → dashboard updates.
5. Admin: read-only Users page; agent gets 403 on it.

## 2. Pipeline (5 min) — Jenkins

1. Open `lastmile-delivery` → `main`: stage view (checkout → validate → tests → Selenium → images →
   registry → deploy).
2. Show the test report (unit + Selenium) and the archived JAR.
3. Show `ci/jenkins-pipeline` builds #9 (red Selenium gate, deploy skipped) and #10 (fixed).
4. Show build #15 (fault injection → automatic rollback).
5. Show `develop` build #8 for `27b6e96241076b72ea464ce8dbf8bf94ff14c174`: 147 unit/integration and 11 Selenium tests,
   digest-verified `27b6e9624107` app/Nginx images, and Deploy skipped. This is a CI candidate, not a deployment.

## 3. Containers and registry (3 min)

```powershell
curl.exe http://127.0.0.1:5000/v2/lastmile-delivery/tags/list
docker inspect lastmile-app-1 --format '{{.Config.Image}} {{.Config.User}} {{index .Config.Labels \"org.opencontainers.image.revision\"}}'   # single quotes outside: works in Windows PowerShell 5.1
git rev-parse "v1.0.0^{commit}"      # ff1cac19ee86cfde8ca09b6019222fbd6abdb674 = the revision label above
```

## 4. Ansible (3 min) — Ubuntu/WSL terminal, repository root

Current release on both targets (verified 2026-10-23): `ff1cac19ee86` (= `v1.0.0`). The target's
release records read `current=ff1cac19ee86`, `previous=e3b9d31cae5b` (`v0.1.0`). Its Nginx listens on
`127.0.0.1:8089` (8088 is the workstation deployment); it also answers from Windows at
http://127.0.0.1:8089.

```bash
# separate window first, keeps the target running:  wsl -d lastmile-target -u root -- sleep infinity
bash scripts/wsl-target/run-ansible.sh ansible -m ansible.builtin.ping lastmile_targets          # expect: pong
bash scripts/wsl-target/run-ansible.sh ansible-playbook site.yml -e lastmile_image_tag=ff1cac19ee86   # idempotency: expect changed=0
curl -s http://127.0.0.1:8089/api/v1/health/ready                                               # expect: {"status":"UP"}
```

**Optional — intentional version change (only if time allows).** This *changes the running version on the
target* to the previous release `e3b9d31cae5b` (`v0.1.0`) and must be followed by the roll-forward so the
target ends on `v1.0.0` again. Database contents are not changed by either step (proven in
`docs/evidence/phase-15-release/target-state.txt`). The workstation is not affected.

```bash
bash scripts/wsl-target/run-ansible.sh ansible-playbook rollback.yml                             # INTENTIONAL: v1.0.0 -> v0.1.0 (e3b9d31cae5b), expect changed=4
bash scripts/wsl-target/run-ansible.sh ansible-playbook site.yml -e lastmile_image_tag=ff1cac19ee86   # ROLL FORWARD: back to v1.0.0, expect changed=4
```

If you skip the optional part, show the recorded run instead: `docs/evidence/phase-15-release/release-v1.0.0-evidence.md`
(upgrade `changed=4`, second run `changed=0`, rollback and roll-forward) and
`docs/evidence/phase-13-ansible/target-run-evidence.md` (first run `changed=13`, failed-candidate drill).
On the target choice: the teacher said "Do whatever you think is right and best for your system."; the
target is a separate WSL2 distribution, not a separate VM (D20).

## 5. Git (1 min)

PRs #8–#19 with their self-review notes, `demo/conflict-*` resolution, releases `v0.1.0` and `v1.0.0` (`main` = `v1.0.0`).

## If something fails during the demo

Show the failure honestly, then the relevant row of `docs/TROUBLESHOOTING.md`. Readiness `DOWN`
means the database is unreachable; `docker compose -p lastmile logs app --tail 50` shows why.
