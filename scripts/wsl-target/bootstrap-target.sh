#!/usr/bin/env bash
# Minimal, idempotent bootstrap of the dedicated WSL2 Ansible target `lastmile-target` (Option B).
# Runs as root INSIDE the target, fed through stdin (the target has no access to Windows drives):
#   wsl -d lastmile-target -u root -- bash -s -- "<ssh public key>" < scripts/wsl-target/bootstrap-target.sh
# It only provides what Ansible needs to connect; everything else is configured by ansible/site.yml.
set -euo pipefail
PUBKEY="${1:?usage: bootstrap-target.sh '<ssh-ed25519 public key>'}"
case "$PUBKEY" in ssh-ed25519\ *) ;; *) echo "expected an ssh-ed25519 public key" >&2; exit 2 ;; esac

# Isolation and systemd. automount/interop off: the target cannot read Windows drives or run Windows
# programs; Ansible reaches it only over SSH.
cat > /etc/wsl.conf <<'EOF'
[boot]
systemd=true

[user]
default=deploy

[network]
hostname=lastmile-target
generateHosts=true

[automount]
enabled=false
mountFsTab=false

[interop]
enabled=false
appendWindowsPath=false
EOF

export DEBIAN_FRONTEND=noninteractive
apt-get update -qq
apt-get install -y -qq --no-install-recommends openssh-server sudo python3 ca-certificates >/dev/null

# Provisioning account: key-only login, passwordless sudo for Ansible's become (it manages packages
# and services). No password is set, so the account cannot log in with one.
id deploy >/dev/null 2>&1 || useradd --create-home --shell /bin/bash deploy
passwd -l deploy >/dev/null
install -d -m 700 -o deploy -g deploy /home/deploy/.ssh
printf '%s\n' "$PUBKEY" > /home/deploy/.ssh/authorized_keys
chown deploy:deploy /home/deploy/.ssh/authorized_keys
chmod 600 /home/deploy/.ssh/authorized_keys
printf 'deploy ALL=(ALL) NOPASSWD:ALL\n' > /etc/sudoers.d/90-deploy
chmod 440 /etc/sudoers.d/90-deploy
visudo -cf /etc/sudoers.d/90-deploy >/dev/null

# SSH: loopback only (the WSL network stack is shared with the control node), port 2222, keys only.
cat > /etc/ssh/sshd_config.d/10-lastmile.conf <<'EOF'
Port 2222
ListenAddress 127.0.0.1
PasswordAuthentication no
KbdInteractiveAuthentication no
PermitRootLogin no
AllowUsers deploy
EOF
install -d -m 755 /run/sshd   # normally created by systemd; needed for the syntax check below
sshd -t
echo "bootstrap complete; restart the distribution so systemd and wsl.conf take effect"
