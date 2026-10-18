# Contributing

Branch naming, commit style, PR rules and the conflict-demo procedure are defined in
[docs/GIT_WORKFLOW.md](docs/GIT_WORKFLOW.md). Definition of Done: [docs/project-board.md](docs/project-board.md#definition-of-done).

Before opening a PR:

1. `mvnw.cmd -B verify` (PowerShell) or `./mvnw -B verify` (Ubuntu/WSL) passes.
2. No secrets or local files are staged: `git diff --cached --name-only` and review the diff.
3. Fill in the PR template, including the exact test results.

Use fictional data only. Never commit `.env`, keys, passwords or tokens.
