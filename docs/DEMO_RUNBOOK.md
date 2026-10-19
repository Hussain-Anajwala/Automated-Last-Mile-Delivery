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

## 3. Containers and registry (3 min)

```powershell
curl.exe http://127.0.0.1:5000/v2/lastmile-delivery/tags/list
docker inspect lastmile-app-1 --format "{{.Config.Image}} {{.Config.User}} {{index .Config.Labels \"org.opencontainers.image.revision\"}}"
git log -1 --format=%H main          # equals the revision label after a main deployment
```

## 4. Ansible (2 min)

Today: show `ansible/`, `docs/ANSIBLE.md`, the static checks and the read-only validation run.
After the target exists (B4): run 1, run 2 (`changed=0`), `rollback.yml`.

## 5. Git (1 min)

PRs #8–#14 with review notes, `demo/conflict-*` resolution, tag `v0.1.0`.

## If something fails during the demo

Show the failure honestly, then the relevant row of `docs/TROUBLESHOOTING.md`. Readiness `DOWN`
means the database is unreachable; `docker compose -p lastmile logs app --tail 50` shows why.
