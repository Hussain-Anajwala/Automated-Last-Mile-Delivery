#!/usr/bin/env bash
# Ubuntu/WSL (control node). Runs Ansible against the Option B target `lastmile-target`.
# Secrets are loaded from ~/.config/lastmile/target-secrets.env (mode 600, outside the repository,
# never printed). Usage from the repository root:
#   bash scripts/wsl-target/run-ansible.sh ansible -m ansible.builtin.ping lastmile_targets
#   bash scripts/wsl-target/run-ansible.sh ansible-playbook site.yml -e lastmile_image_tag=<sha12>
set -euo pipefail
REPO="$(cd "$(dirname "$0")/../.." && pwd)"
SECRETS="${LASTMILE_TARGET_SECRETS:-$HOME/.config/lastmile/target-secrets.env}"
[ -f "$SECRETS" ] || { echo "missing $SECRETS (see docs/ANSIBLE_TARGET_SETUP.md)" >&2; exit 2; }
# shellcheck disable=SC1090
source "$SECRETS"
TOOL="${1:?usage: run-ansible.sh <ansible|ansible-playbook> [args]}"; shift
exec bash "$REPO/scripts/ansible-run.sh" "$TOOL" -i inventory/wsl-target.yml "$@"
