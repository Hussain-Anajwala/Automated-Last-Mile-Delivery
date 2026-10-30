# Phases 13–14 — Ansible on the Option B target (2026-10-19)

**Technical status:** all six acceptance criteria below passed on the dedicated WSL2 distribution
`lastmile-target` (approved by the user). **Academic status (as written on the day of the run):** whether a
WSL2 distribution is acceptable as the "clean node" was still awaiting the teacher's confirmation.
*Update 2026-10-19:* the teacher answered "Do whatever you think is right and best for your system."; the
WSL2 target was kept (decision D20). The target now runs `v1.0.0` (`ff1cac19ee86`) — see
`docs/evidence/phase-15-release/release-v1.0.0-evidence.md`.

Control node: `Ubuntu-24.04` (WSL2), ansible-core 2.21.5, run through `scripts/wsl-target/run-ansible.sh`
(secrets loaded from `~/.config/lastmile/target-secrets.env`, mode 600, never printed).
Logs in this folder are complete `ansible-playbook` outputs with their exit codes; `no_log` hides secrets.

## Target creation (Option B)

| Step | Result |
|---|---|
| Root filesystem | `ubuntu-noble-wsl-amd64-wsl.rootfs.tar.gz` from `cloud-images.ubuntu.com/wsl/releases/24.04/current/`; `gpgv` → *Good signature from "UEC Image Automatic Signing Key"*; `sha256sum -c` → OK |
| Import | `wsl --import lastmile-target D:\WSL\lastmile-target … --version 2` → success; `Ubuntu-24.04` untouched and still default |
| Bootstrap | `scripts/wsl-target/bootstrap-target.sh` (first run stopped at `sshd -t` because `/run/sshd` is created by systemd — fixed, re-run exit 0) |
| Isolation | systemd PID 1, hostname `lastmile-target`; Windows drives not mounted; no Windows interop; SSH `127.0.0.1:2222` key-only (password login refused), `PermitRootLogin no`, `AllowUsers deploy`; host key `SHA256:E8C9W/PsMiDBdST2O4qBWHhgnyH4eCCHxNNMIgPQ1V8` |
| Shared network stack | target `eth0` = `172.30.142.180/20`, same as the control node — hence port 8089, firewall off, own Docker ranges `10.213.0.0/16` |
| Registry from the target itself | `GET http://localhost:5000/v2/` → 200; tags listed; `localhost` → `127.0.0.1` only |
| Keep-alive | WSL stopped the idle distro (SSH refused); a `wsl -d lastmile-target -u root -- sleep infinity` session keeps it running during work |

## Acceptance criteria

| # | Criterion | Result | Evidence |
|---|---|---|---|
| — | Pre-checks before changes | syntax 0; ansible-lint 0 (production profile); render-and-validate 0 (`ok=10 changed=0`); negative control 2 (`failed=1`) | session log |
| — | Dry run `--check --diff` | `ok=21 changed=10 failed=0 ignored=3`; target unchanged afterwards (`/opt/lastmile` absent, no docker) | `01-check-mode.log` |
| 1 | Reachable, `pong` | `lastmile-target | SUCCESS => ping: pong`; Ubuntu 24.04 (noble) | session log |
| 2 | Packages, account, directories, files, services | run 1 `ok=25 changed=13 failed=0`; independently verified | `02-run1-release-A.log`, `03-verify-after-run1.txt` |
| 3 | Application deployed | `localhost:5000/lastmile-delivery:0ac1a53c836f` + nginx + postgres healthy; image digests on the target equal the registry's (`sha256:561e1425…`, `sha256:eb4cedc2…`) | `03-verify-after-run1.txt` |
| 4 | Health of the deployed version | via the target's Nginx `{"status":"UP"}` HTTP 200; revision label `0ac1a53c836f3e29…` = requested tag | `03-verify-after-run1.txt` |
| 5 | Idempotency (same playbook again) | **`ok=24 changed=0 failed=0`**; containers not restarted | `04-run2-idempotency.log` |
| 6 | Controlled recovery to previous stable release | upgrade to `e3b9d31cae5b` (v0.1.0; 4 expected changes; digest `sha256:64eb5a33…` = Jenkins main #1), then `rollback.yml` → back to `0ac1a53c836f`, `healthy`, readiness 200; records `current=0ac1a53c836f previous=e3b9d31cae5b`; database rows from run 1 (6 deliveries, 15 history) still present | `05-release-B.log`, `06-rollback-to-previous.log` |
| 6b | Failed-candidate drill | candidate `e3b9d31cae5b` with `lastmile_app_db_host=db-does-not-exist` → `dependency failed to start: container lastmile-app-1 is unhealthy`, `failed=1`, release records unchanged; `rollback.yml -e lastmile_rollback_record=current` → `0ac1a53c836f` healthy, readiness 200 | `07-failed-candidate.log`, `07b-…`, `08-recover-current.log`, `08b-…` |

## Defects found by the real run and fixed

1. `site.yml`/`rollback.yml` used `lastmile_image_tag | regex_search(...)` in `assert`; ansible-core 2.21
   rejects non-boolean conditionals. Syntax check and lint did not catch it, and the offline test
   checked an equivalent rule instead of the exact expression. Fixed with `is regex(...)`, and the
   offline test now asserts the identical expression.
2. `--check` on a fresh target cannot install Docker (the repository file is not really written), so
   the Docker install, service and restart handler tolerate errors **only in check mode**, and the
   deployment tasks (pull, up, readiness, revision) are skipped in check mode. The dry run therefore
   covers configuration, not deployment.
3. Bootstrap `sshd -t` needed `/run/sshd` before systemd runs.

## Limits (stated, not hidden)

- The target shares the WSL kernel and network stack with Docker Desktop and the control node; it is
  a separate root filesystem, init system, SSH server and Docker Engine, not a separate machine.
- Host firewall (ufw) is not enabled on this target (it would affect every WSL distribution).
- Rollback restores image and configuration; database contents are not rolled back (forward-only
  migrations; V1 only so far).
