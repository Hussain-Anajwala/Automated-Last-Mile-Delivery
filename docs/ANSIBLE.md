# Configuration Management with Ansible (Teacher Tasks 13–14)

## Control node and target

| Role | What | Status |
|---|---|---|
| Control node | Ubuntu 24.04 under WSL2, `ansible-core 2.21.5` + `ansible-lint` in `~/.venvs/ansible` (`scripts/bootstrap-ansible-wsl.sh`, no sudo) | Ready |
| Target | Dedicated WSL2 distribution `lastmile-target` (Option B, approved by the user 2026-10-19), SSH `127.0.0.1:2222`, inventory `ansible/inventory/wsl-target.yml` | **Provisioned and verified technically**; teacher acceptance of WSL2 pending (B6 Q3) |

The playbook refuses to run on anything that is not Ubuntu and requires an explicit image tag.
It must never target the Windows workstation.

## Configuration specification (what the playbook guarantees on the target)

| Area | Desired state | Role |
|---|---|---|
| OS | Ubuntu (asserted), timezone `Etc/UTC` | `base` |
| Packages | `ca-certificates curl gnupg ufw python3-requests` | `base` |
| Firewall | ufw enabled, default deny incoming, SSH allowed (allowed *before* enabling) | `base` |
| Container runtime | Docker Engine + Compose plugin from Docker's official apt repository; service enabled and running | `docker_engine` |
| Docker daemon | `/etc/docker/daemon.json`: log rotation 10 MB × 3; the host-only registry listed as `insecure-registries` (plain HTTP on a trusted network only); restart via handler only when the file changes | `docker_engine` |
| Service account | system group/user `lastmile`, no login shell, **not** in `docker` (that group is root-equivalent) | `lastmile_app` |
| Directories | `/opt/lastmile` and `/opt/lastmile/releases`, `root:lastmile 0750` | `lastmile_app` |
| Files | `/opt/lastmile/compose.yaml` (same file as the Jenkins pipeline, `0640`); `/opt/lastmile/.env` with runtime config and secrets (`root:root 0600`, `no_log`) | `lastmile_app` |
| Ports | Nginx on `127.0.0.1:8088` of the target; app and database unpublished | compose |
| Release | images `<registry>/lastmile-delivery:<sha12>` and `<registry>/lastmile-nginx:<sha12>` pulled by exact tag; `up --wait`; readiness checked via Nginx; deployed revision checked against the tag | `lastmile_app` |
| Release record | `/opt/lastmile/releases/current` and `previous` (rollback = rerun with the previous tag) | `lastmile_app` |

Secrets (`LASTMILE_DB_PASSWORD`, `LASTMILE_DEMO_PASSWORD`) come from the control node's environment
(or a git-ignored ansible-vault file), are asserted to exist, and are written only to the `0600`
file with `no_log`.

Idempotency: every `command` task declares `changed_when`, so a second run with the same tag
reports `changed=0` — demonstrated on the target (run 2, see below).

## Commands (Ubuntu/WSL, repository root)

Ansible ignores `ansible.cfg` in world-writable directories such as `/mnt/c/...`, so
`scripts/ansible-run.sh` copies `ansible/` and `deploy/` into `~/lastmile-ansible` and runs there.

```bash
cp ansible/inventory/hosts.example.yml ansible/inventory/hosts.local.yml   # edit: real target
bash scripts/ansible-run.sh ansible-playbook site.yml --syntax-check
bash scripts/ansible-run.sh ansible-lint --offline site.yml roles
bash scripts/ansible-run.sh ansible -m ansible.builtin.ping lastmile_targets
export LASTMILE_DB_PASSWORD=... LASTMILE_DEMO_PASSWORD=...     # never commit these
bash scripts/ansible-run.sh ansible-playbook site.yml --check --diff -e lastmile_image_tag=<sha12>
bash scripts/ansible-run.sh ansible-playbook site.yml -e lastmile_image_tag=<sha12>      # run 1
bash scripts/ansible-run.sh ansible-playbook site.yml -e lastmile_image_tag=<sha12>      # run 2: expect changed=0
```

Rollback: `-e lastmile_image_tag=$(ssh <target> cat /opt/lastmile/releases/previous)`.

## Static checks run so far (2026-10-19)

| Check | Result |
|---|---|
| `ansible-playbook site.yml --syntax-check` | `playbook: site.yml`, exit 0 |
| `ansible-lint --offline -p site.yml rollback.yml roles tests` (26.10.1) | exit 0, `production` profile (after fixing 15 `var-naming` and 1 `line-length` findings) |
| `tests/render-and-validate.yml` (read-only, localhost) | exit 0, `ok=8 changed=0 failed=0`; negative control with tag `latest` → exit 2, `failed=1` |
| `rollback.yml --syntax-check` | OK |

PR review fixes: removed the unused `lastmile_http_bind` variable; the `lastmile` account is no longer
in the `docker` group (root-equivalent and unnecessary).

Rollback: `bash scripts/ansible-run.sh ansible-playbook rollback.yml` redeploys the tag recorded in
`/opt/lastmile/releases/previous`. Target preparation runbooks: [ANSIBLE_TARGET_SETUP.md](ANSIBLE_TARGET_SETUP.md).

## Execution on the target (2026-10-19)

All six acceptance criteria passed on `lastmile-target`: `pong`; run 1 `changed=13`; deployed release
healthy with registry-identical digests; run 2 **`changed=0`**; upgrade then `rollback.yml` back to the
previous release (healthy); failed-candidate drill recovered with `-e lastmile_rollback_record=current`.
Details and logs: [evidence/phase-13-ansible/target-run-evidence.md](evidence/phase-13-ansible/target-run-evidence.md).

Check mode (`--check --diff`) covers configuration only: on a fresh target it cannot install Docker
(the apt repository file is not really written), so those tasks tolerate errors in check mode only, and
the deployment tasks are skipped in check mode.

Wrapper for this target (loads secrets from `~/.config/lastmile/target-secrets.env`):
`bash scripts/wsl-target/run-ansible.sh ansible-playbook site.yml -e lastmile_image_tag=<sha12>`.
