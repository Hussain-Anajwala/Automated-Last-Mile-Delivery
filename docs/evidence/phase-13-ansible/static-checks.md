# Phase 13 — checks possible without a managed host (2026-10-19)

Run from Ubuntu 24.04 (WSL) via `scripts/ansible-run.sh` (ansible-core 2.21.5, ansible-lint 26.10.1).
No playbook has been executed against a managed host.

```text
syntax-check site.yml: exit 0
ansible-lint (site, roles, tests): exit 0 — 
render-and-validate: exit 0 — ok=8    changed=0    unreachable=0    failed=0
negative control (tag=latest): exit 2 — ok=4    changed=0    unreachable=0    failed=1
rollback.yml --syntax-check: OK
```

`tests/render-and-validate.yml` renders the real templates with sample values on localhost using only
template lookups, assert and debug (changed=0); the negative control (`-e lastmile_image_tag=latest`)
must fail and does (exit 2, failed=1).
