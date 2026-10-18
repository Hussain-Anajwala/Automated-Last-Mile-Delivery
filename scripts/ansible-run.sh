#!/usr/bin/env bash
# Ubuntu/WSL (bash). Runs Ansible tooling from a copy of ansible/ and deploy/ in the Linux filesystem.
#
# Why: on the Windows mount (/mnt/c/...) every directory looks world-writable, so Ansible silently
# ignores ansible.cfg there (including host-key checking and the inventory path).
#
# Usage (from the repository root inside WSL):
#   bash scripts/ansible-run.sh ansible-playbook site.yml --syntax-check
#   bash scripts/ansible-run.sh ansible-lint --offline site.yml roles
#   bash scripts/ansible-run.sh ansible-playbook site.yml -e lastmile_image_tag=<sha12>
# The git-ignored inventory/hosts.local.yml is copied along with the rest of ansible/.
set -euo pipefail
REPO="$(cd "$(dirname "$0")/.." && pwd)"
WORK="${LASTMILE_ANSIBLE_WORKDIR:-$HOME/lastmile-ansible}"
VENV="${LASTMILE_ANSIBLE_VENV:-$HOME/.venvs/ansible}"
TOOL="${1:?usage: scripts/ansible-run.sh <ansible-playbook|ansible|ansible-lint|...> [args]}"
shift

mkdir -p "$WORK"
rm -rf "$WORK/ansible" "$WORK/deploy"
cp -r "$REPO/ansible" "$REPO/deploy" "$WORK/"
chmod -R go-w "$WORK"
cd "$WORK/ansible"
export ANSIBLE_CONFIG="$WORK/ansible/ansible.cfg"
exec "$VENV/bin/$TOOL" "$@"
