# Evidence Index and Redaction Rules

Evidence is grouped by phase: `docs/evidence/phase-NN-<name>/`. The teacher-task-to-evidence map is
[`INDEX.md`](INDEX.md).

## Rules

- Only record output that was actually observed. Anything not run is marked `BLOCKED/NOT RUN`.
- Redact before committing: passwords, tokens, private keys, personal email addresses, private
  IP addresses/hostnames of personal machines where not needed, and any non-synthetic personal data.
- Keep large logs out of Git; store key excerpts and note where the full log lives.
- Screenshots must not show credentials, tokens or real personal data.

## Index

| Phase | Evidence |
|---|---|
| 0 — Audit | [phase-00-audit/environment-audit.md](phase-00-audit/environment-audit.md) |
| 3 — Setup | [phase-03-setup/compatibility-gate.md](phase-03-setup/compatibility-gate.md) |
| 4 — Repository | [phase-04-repo/repository-init.md](phase-04-repo/repository-init.md) |
| 5 — First feature | [phase-05-feature/feature-evidence.md](phase-05-feature/feature-evidence.md), [pull-request.md](phase-05-feature/pull-request.md) |
| 6 — MVP | [phase-06-mvp/acceptance.md](phase-06-mvp/acceptance.md), [merge-conflict-transcript.md](phase-06-mvp/merge-conflict-transcript.md), [screenshots/](phase-06-mvp/screenshots/) |
| 7 — Jenkins CI | [phase-07-jenkins/jenkins-evidence.md](phase-07-jenkins/jenkins-evidence.md) |
| 8 — Deployment | [phase-08-deploy/deploy-evidence.md](phase-08-deploy/deploy-evidence.md) |
| 9 — Selenium | [phase-09-selenium/selenium-evidence.md](phase-09-selenium/selenium-evidence.md) |
| 10 — Quality gate | [phase-10-quality-gate/quality-gate-evidence.md](phase-10-quality-gate/quality-gate-evidence.md) |
| 11 — Docker | [phase-11-docker/lifecycle-transcript.md](phase-11-docker/lifecycle-transcript.md) |
| 12 — Registry/CD | [phase-12-registry/registry-evidence.md](phase-12-registry/registry-evidence.md), `main-1-release-deploy.log`, `develop-1-no-deploy.log` |
| 13 — Ansible (static only) | [phase-13-ansible/static-checks.md](phase-13-ansible/static-checks.md) |
| 5–6 — PRs, merges, release | [phase-06-mvp/pull-requests-and-release.md](phase-06-mvp/pull-requests-and-release.md) |
| 15 — Release (in progress) | [INDEX.md](INDEX.md), `phase-15-release/memory-during-main-build.txt` |
