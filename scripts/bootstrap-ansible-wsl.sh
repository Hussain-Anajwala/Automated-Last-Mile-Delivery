#!/usr/bin/env bash
# Ubuntu/WSL (bash): bootstraps a user-level Ansible control environment without sudo.
# python3-venv/pip are not installed by default on Ubuntu 24.04 WSL; the pip wheel is fetched from
# PyPI and its SHA-256 is checked against the digest PyPI publishes before use.
# Usage: bash scripts/bootstrap-ansible-wsl.sh   then   ~/.venvs/ansible/bin/ansible --version
set -euo pipefail
mkdir -p ~/.cache/lastmile && cd ~/.cache/lastmile
read -r URL SHA < <(python3 -c 'import json,urllib.request
d=json.load(urllib.request.urlopen("https://pypi.org/pypi/pip/json"))
w=[u for u in d["urls"] if u["packagetype"]=="bdist_wheel"][0]
print(w["url"], w["digests"]["sha256"])')
F=$(basename "$URL")
curl -sSfL -o "$F" "$URL"
ACTUAL=$(sha256sum "$F" | cut -d' ' -f1)
echo "pip wheel: $F"
echo "pypi sha256:   $SHA"
echo "actual sha256: $ACTUAL"
[ "$SHA" = "$ACTUAL" ] || { echo "checksum MISMATCH"; exit 1; }
echo "checksum MATCH"
rm -rf ~/.venvs/ansible
python3 -m venv --without-pip ~/.venvs/ansible
~/.venvs/ansible/bin/python "$F/pip" install -q --no-index "$F"
~/.venvs/ansible/bin/python -m pip --version
~/.venvs/ansible/bin/python -m pip install -q "ansible-core==2.21.5"
~/.venvs/ansible/bin/ansible --version | head -3
