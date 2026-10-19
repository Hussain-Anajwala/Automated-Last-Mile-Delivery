# Troubleshooting

Problems actually met during this project, with the fix that worked.

| Symptom | Cause | Fix |
|---|---|---|
| `mvnw.cmd` uses Java 22 or fails | `JAVA_HOME` not set to JDK 21 | `. .\scripts\use-jdk21.ps1` (sets it for the session) |
| `./mvnw: Permission denied` in Jenkins/Linux | File committed without the executable bit | `git update-index --chmod=+x mvnw` (done) |
| Docker build ends with `rpc error … EOF` | The WSL VM restarted (Docker Desktop update, or Windows commit limit exhausted) | Check `docker version`; close heavy apps; rerun one build at a time (BLOCKERS B7) |
| Jenkins build ends with `script returned exit code -1` | Jenkins container was recreated during the build | Don't restart Jenkins while busy; check `/computer/api/json?tree=busyExecutors` first |
| Two pipelines starve memory | Two executors | Jenkins now has `numExecutors: 1` |
| Selenium: `element click intercepted` | Bootstrap smooth scrolling races WebDriver's scroll | Base class scrolls instantly before clicking |
| Selenium: `Node … does not belong to the document` | Chrome reports a node from the previous page with a generic error | Base class waits for a new document via a `window` marker |
| Selenium in Jenkins: Chrome won't start | Sandbox unavailable in an unprivileged container | `-Dselenium.noSandbox=true` plus Debian's chromium/chromedriver paths (see `Jenkinsfile`) |
| Redirect after login goes to port 80 | Nginx sent the container port in `Host` | `proxy_set_header Host $http_host` (done) |
| `docker compose` refuses to start: `LASTMILE_IMAGE_TAG must be set` | Required variables are guarded on purpose | Provide the tag and DB password (see `docs/DEPLOYMENT.md`) |
| Bind-mounted config not found when Jenkins deploys | Paths resolve on the Docker host, not in the Jenkins workspace | Nginx config is baked into the `lastmile-nginx` image |
| Ansible ignores `ansible.cfg` | `/mnt/c/...` looks world-writable to Ansible | Use `scripts/ansible-run.sh` (runs from the Linux filesystem) |
| `python3 -m venv` fails in Ubuntu WSL | `python3-venv`/`pip` not installed and no sudo | `scripts/bootstrap-ansible-wsl.sh` (venv without pip + verified pip wheel) |
| Git Bash mangles `branch:path` or `/mnt/c/...` arguments | MSYS path conversion | prefix the command with `MSYS_NO_PATHCONV=1` |
| Secret scan "passed" but output said leaks found | Exit status of a pipe is the last command's | Never pipe the gate; use `scripts/secret-scan.sh`/`safe-push.sh` directly |
| Readiness `503 {"status":"DOWN"}` while liveness is 200 | Database unreachable (by design) | Check `docker compose -p lastmile ps` and the `db` container |
