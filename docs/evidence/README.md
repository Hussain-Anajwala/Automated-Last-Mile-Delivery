# Evidence Index and Redaction Rules

Evidence is grouped by phase: `docs/evidence/phase-NN-<name>/`. The full teacher-task-to-evidence
map will be `docs/evidence/INDEX.md` (Phase 15).

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
