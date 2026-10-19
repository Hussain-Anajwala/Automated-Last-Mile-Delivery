# Preparing the Ansible Target (BLOCKERS B4)

**Nothing in this file has been executed.** The target type needs the user's decision and approval
(and, for Option B, the teacher's acceptance). Each step lists who acts and how to verify it.

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
| 1 | Claude | Download the official Ubuntu 24.04 WSL root filesystem from `cloud-images.ubuntu.com/wsl/releases/24.04/current/` and verify it against the published `SHA256SUMS`. |
| 2 | Claude | `wsl --import lastmile-target D:\WSL\lastmile-target <rootfs.tar.gz> --version 2` (no admin needed). |
| 3 | Claude | Inside the target as root: create user `deploy` with sudo, enable systemd in `/etc/wsl.conf`, install `openssh-server` listening on port 2222, install the public key; restart the distro. |
| 4 | User (check) | Docker Desktop → *Settings → Resources → WSL integration*: integration **off** for `lastmile-target`, so the playbook installs its own Docker Engine there. |
| 5 | Claude | Inventory: `ansible_host: 127.0.0.1`, `ansible_port: 2222`, `lastmile_http_port: 8089` (8088 is used by the workstation stack in the shared WSL network), `base_manage_firewall: false`, registry `localhost:5000` if reachable from the target. |

Known limits of Option B, to state in the report: shares the WSL kernel and network stack with Docker
Desktop and the control node (not a separate machine); ufw stays off because the firewall would affect
every WSL distribution. A probe from the existing Ubuntu distro on 2026-10-19 showed
`http://localhost:5000/v2/` → 200, so pulling the same `localhost:5000/...` reference is likely but
must be verified from the target itself.

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
