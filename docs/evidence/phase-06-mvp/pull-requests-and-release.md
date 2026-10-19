# Pull requests, merges and release (2026-10-19)

Created and merged with `gh` as the repository owner. Every PR carries an author self-review performed by the
Claude Code assistant and is labelled **not an independent review** (decision D15). No approval was faked.

```text
#8 MERGED feature/delivery-create -> develop merge=0820c84 feat: delivery creation with validation, generated codes and audit history
#9 MERGED feature/delivery-workflow -> develop merge=94756f8 feat: complete the MVP — search, edit, assignment, status workflow, dashboard, API
#10 MERGED test/selenium-journeys -> develop merge=d4c0d27 test: five Selenium journeys with failure artifacts and reports
#11 MERGED ci/jenkins-pipeline -> develop merge=c719e9f ci: Jenkins pipeline, Docker images, local registry, deployment and rollback
#12 MERGED infra/ansible-provisioning -> develop merge=ad3af20 infra: Ansible roles, rollback and offline validation for the Ubuntu target
#13 MERGED ci/secret-gate-hardening -> develop merge=03f6bb1 ci: fail-closed secret gate and one Jenkins executor
#14 MERGED ci/deploy-from-main -> develop merge=7af50fe ci: deploy only from main
#15 MERGED develop -> main merge=e3b9d31 release: v0.1.0 MVP baseline
```

Merged-result verification: `tmp/release-check` (local) = exact merge of develop + #10–#13, tree
`c2f4ff68`, passed `mvnw.cmd -B clean verify -Pselenium` (89 + 8). After the GitHub merges, `origin/develop`
had the identical tree (`git rev-parse origin/develop^{tree}` = `c2f4ff68…`); `main` (#15) tree = develop tree.

Jenkins: develop #1/#2 at `7af50fe` — 89 + 8 passed, published, `Stage "Deploy" skipped due to when conditional`;
main #1 at `e3b9d31` — 89 + 8 passed, published (digests match), deployed, readiness UP, revision verified.

Tag: `v0.1.0` (annotated) → `e3b9d31cae5bd48eb1bfbaf3db56e21f0ddc56e3`, pushed via `scripts/safe-push.sh` after a clean history scan.
GitHub release: https://github.com/Hussain-Anajwala/Automated-Last-Mile-Delivery/releases/tag/v0.1.0

Review findings fixed in this round: #9 server-side search length + 400 mapping (`566904b`); #12 unused variable and
docker-group membership (`c18e53f`); #14 deploy only from main.
