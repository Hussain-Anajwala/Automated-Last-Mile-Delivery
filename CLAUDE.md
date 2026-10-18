# Project Instructions — Automated Last-Mile Delivery Management Platform

## Source of truth

Before making changes, read `README.md`, `docs/PRD.md`, `docs/TRD.md`, `docs/UI_UX_Design.md`, `docs/App_Flow.md`, `docs/Backend_Schema.md`, `docs/Implementation_Plan.md`, and `MASTER_PROMPT_CLAUDE_CODE.md`. The planning documents and approved decision log are authoritative. If they conflict, record the exact conflict and resolve it using verified requirements rather than guessing.

## Approved stack

Java 21 LTS; compatible Spring Boot; Spring MVC/Thymeleaf/Bootstrap; Spring Security; Spring Data JPA; PostgreSQL; Flyway; Maven Wrapper; JUnit 5; Selenium; Jenkins Pipeline-as-Code; Docker/Compose; local Docker Registry; Nginx; Ansible controlled from Ubuntu/WSL2 and applied to an isolated Ubuntu target. UI design is code-first; do not introduce a dependency on Google Stitch. Preserve the teacher's 15-task sequence and evidence requirements.

## Working rules

- Inspect the current directory, `git status`, current branch, remotes, untracked files, and existing modifications before editing anything. Never discard user changes.
- Work one teacher phase at a time. Automatically proceed to the next phase after passing its exit gate; do not ask for approval after every successful phase.
- Run real commands and tests. Report exact commands, exit codes, and relevant outcomes. Never claim an unrun test, unobserved deployment, PR, GitHub review, registry publication, idempotency result, or rollback succeeded.
- Verify changing technical facts and version compatibility against official documentation where network access is available. Pin selected dependency/plugin versions and document why they were selected.
- Keep logic in services, authorization on the server, system-managed fields out of ordinary client input, secrets out of Git, and test data fictional.
- Keep changes scoped and maintainable. Add regression tests for fixes. Update docs when behavior or commands change.
- Use named feature branches and meaningful commits. Never force-push, delete a remote branch, amend another person's commit, reset away work, or change repository visibility/settings without explicit authorization.
- Create GitHub issues/PRs only when the configured remote and `gh` authentication are confirmed. Never invent an independent reviewer or review comments. Report any human review that the course specifically requires.
- Use Windows PowerShell only for Windows-specific commands; label Ubuntu/WSL commands explicitly. Prefer running Claude Code in Ubuntu under WSL2 from the Linux filesystem for a consistent Bash/Ansible toolchain.
- Pause only for missing credentials/privileged setup, a destructive or externally consequential action not clearly authorized, a genuine teacher-specific ambiguity, or a blocker that cannot be responsibly resolved. Continue independent work and record exact user actions needed.
- Do not run with `--dangerously-skip-permissions`. Prefer Claude Code's normal permission controls or supported auto mode, and allow only commands that have been reviewed and understood.

## Required handover

Maintain `docs/IMPLEMENTATION_LOG.md`, `docs/DECISIONS_LOG.md`, and `docs/BLOCKERS.md` during implementation. At each phase boundary, record scope, files, commands, test evidence, commits/PRs/tags, known issues, remaining user actions, and pass/block status. Save redacted evidence under `docs/evidence/` where practical. A blocked phase must remain explicitly blocked; do not represent it as complete.
