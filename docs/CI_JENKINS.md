# Jenkins (Teacher Tasks 7, 8, 10, 12)

## Topology (decision D10)

A single Jenkins controller in a container on Docker Desktop, defined entirely in Git:

| File | Purpose |
|---|---|
| `ci/jenkins/Dockerfile` | `jenkins/jenkins:2.580.1-lts-jdk21` + Docker CLI/Compose (Docker's Debian repo) + Chromium/chromedriver (Debian) |
| `ci/jenkins/plugins.txt` | Top-level plugins pinned to the 2.580.1 update-center versions |
| `ci/jenkins/casc.yaml` | Configuration as Code: local admin user, no anonymous access, the `lastmile-delivery` multibranch job |
| `ci/jenkins/compose.yaml` | Runs the controller on `127.0.0.1:8090` with a persistent `jenkins_home` volume and the host Docker socket |
| `Jenkinsfile` | The pipeline itself (pipeline as code) |

Security notes:

- UI bound to `127.0.0.1` only; no agent port; no inbound webhook.
- Admin credentials live only in `ci/jenkins/.env` (git-ignored), created by `scripts/jenkins-init-env.ps1`.
- The Docker socket gives Jenkins root-equivalent control of the Docker VM. Acceptable here only
  because the instance is local and single-user; never expose it.
- The repository is public, so checkout needs no credentials.

## Start / stop (Windows PowerShell, repository root)

```powershell
.\scripts\jenkins-init-env.ps1                              # once: creates ci/jenkins/.env
docker compose -f ci/jenkins/compose.yaml up -d --build     # build image and start
docker compose -f ci/jenkins/compose.yaml logs -f jenkins   # wait for "Jenkins is fully up and running"
docker compose -f ci/jenkins/compose.yaml stop              # stop (data kept in the jenkins_home volume)
```

Open http://localhost:8090 and sign in as `admin` with the password in `ci/jenkins/.env`.

## Trigger

`lastmile-delivery` is a multibranch pipeline over
`https://github.com/Hussain-Anajwala/Automated-Last-Mile-Delivery.git`. It re-scans every
2 minutes (`periodicFolderTrigger`) and builds any branch whose head commit changed and that
contains a `Jenkinsfile`. Builds started this way show `Branch indexing` as their cause. This is
SCM polling; a GitHub webhook would need Jenkins to be reachable from the internet, which is
deliberately avoided.

## Pipeline stages

1. **Checkout** — `checkout scm`; records the commit SHA (image tag = first 12 characters).
2. **Validate parameters** — `HTTP_PORT` must be 1024–65535; fails the build before anything runs.
3. **Build and unit/integration tests** — `./mvnw -B -ntp clean verify` (embedded PostgreSQL).
4. **Selenium quality gate** — five browser journeys in Chromium; JUnit + HTML report + failure screenshots always published.
5. **Package images** — `lastmile-delivery:<sha>` from the tested JAR and `lastmile-nginx:<sha>`.
6. **Deploy** (deployment branches, `DEPLOY=true`) — `docker compose up -d --wait` with credentials injected.
7. **Verify deployment** — readiness through Nginx and deployed revision == commit.
8. **Post** — JUnit always; JAR archived on success; container status and app logs on failure.

Any failure in stages 2–4 skips 5–7, so nothing untested is packaged or deployed
(proven in builds #6 and #9 — see `docs/evidence/phase-08-deploy/` and `phase-10-quality-gate/`).

Parameters: `DEPLOY_PROFILE` (`demo`/`prod`), `HTTP_PORT` (default `8088`), `DEPLOY` (default true).

## Resource note

The workstation runs close to its Windows commit limit (BLOCKERS B7). Building the Jenkins image
crashed the WSL VM twice before succeeding; keep other heavy applications closed during builds.
