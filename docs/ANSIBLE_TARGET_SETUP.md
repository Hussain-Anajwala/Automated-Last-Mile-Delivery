# Preparing the Ansible Target (decisions D18, D20)

**Option B was executed on 2026-10-19** after the user's approval (evidence:
`docs/evidence/phase-13-ansible/target-run-evidence.md`). The teacher's acceptance of a WSL2 target is still
pending. Option A has not been executed.

## Common: control-node SSH key (Ubuntu/WSL, user's own account, no sudo)

```bash
ssh-keygen -t ed25519 -f ~/.ssh/lastmile_target_ed25519 -C "lastmile ansible control" -N ""
cat ~/.ssh/lastmile_target_ed25519.pub        # public key only — installed on the target below
```

The private key stays in `~/.ssh` (mode 600) and is never committed or shared.

## Option A — VirtualBox VM (plan's primary target)

| Step | Who | Action |
|---|---|---|
| 1 | User (admin prompt) | Install VirtualBox. |
| 2 | User | Create VM `lastmile-target`: Ubuntu Server 24.04 LTS, 2 vCPU, 2048 MB RAM, 20 GB disk stored on `D:`; adapter 1 NAT, adapter 2 **Host-only** (`192.168.56.0/24`). Install with OpenSSH server, user `deploy`. |
| 3 | User or Claude | On the VM: put the public key into `/home/deploy/.ssh/authorized_keys`; `sudo` for `deploy` (required for package and service management). |
| 4 | Claude | Publish the registry additionally on the host-only address (`192.168.56.1:5000`, never `0.0.0.0`) and set `lastmile_registry: 192.168.56.1:5000` in the inventory. |

## Option B — dedicated WSL2 distribution `lastmile-target` (recommended for this machine's memory)

Requires explicit user approval (adds a WSL distribution) and teacher acceptance of WSL2 as the
"clean node". Removable at any time with `wsl --unregister lastmile-target` (deletes only that distro).

| Step | Who | Action |
|---|---|---|
| 1 | Claude | Download `ubuntu-noble-wsl-amd64-wsl.rootfs.tar.gz`, `SHA256SUMS`, `SHA256SUMS.gpg` from `cloud-images.ubuntu.com/wsl/releases/24.04/current/`; verify the signature with `gpgv --keyring /usr/share/keyrings/ubuntu-cloudimage-keyring.gpg SHA256SUMS.gpg SHA256SUMS` and the file with `sha256sum -c --ignore-missing SHA256SUMS`. |
| 2 | Claude | `wsl --import lastmile-target D:\WSL\lastmile-target <rootfs.tar.gz> --version 2` (no admin needed). |
| 3 | Claude | `wsl -d lastmile-target -u root -- bash -s -- "$(cat ~/.ssh/lastmile_target_ed25519.pub)" < scripts/wsl-target/bootstrap-target.sh` (user `deploy` with sudo, systemd, SSH 127.0.0.1:2222 key-only, no Windows drives/interop), then `wsl --terminate lastmile-target`. |
| 4 | User (check) | Docker Desktop → *Settings → Resources → WSL integration*: integration **off** for `lastmile-target`, so the playbook installs its own Docker Engine there. |
| 5 | Claude | Inventory `ansible/inventory/wsl-target.yml` (committed, no secrets): 127.0.0.1:2222, port 8089, firewall off, Docker ranges 10.213.0.0/16, registry `localhost:5000` (verified from the target). Secrets: `~/.config/lastmile/target-secrets.env` (mode 600, outside the repo). |
| 6 | Claude | Keep the distro running while working: `wsl -d lastmile-target -u root -- sleep infinity` in a separate session (WSL stops idle distributions, which refuses SSH). |

Known limits of Option B, to state in the report: shares the WSL kernel and network stack with Docker
Desktop and the control node (not a separate machine); ufw stays off because the firewall would affect
every WSL distribution. Verified from the target itself: `http://localhost:5000/v2/` → 200, and the
images it pulled have exactly the registry's digests.

Removal (only if ever wanted): `wsl --unregister lastmile-target` deletes this distribution and nothing else.

## After either option (Claude)

```bash
bash scripts/ansible-run.sh ansible -m ansible.builtin.ping lastmile_targets                 # expect pong
bash scripts/ansible-run.sh ansible-playbook site.yml --check --diff -e lastmile_image_tag=<sha12>
bash scripts/ansible-run.sh ansible-playbook site.yml -e lastmile_image_tag=<sha12>            # run 1
bash scripts/ansible-run.sh ansible-playbook site.yml -e lastmile_image_tag=<sha12>            # run 2: changed=0
bash scripts/ansible-run.sh ansible-playbook site.yml -e lastmile_image_tag=<newer sha12>      # new release
bash scripts/ansible-run.sh ansible-playbook rollback.yml                                       # back to previous
```

Each run's recap is saved under `docs/evidence/phase-13-ansible/` and `phase-14-provisioning/`.
