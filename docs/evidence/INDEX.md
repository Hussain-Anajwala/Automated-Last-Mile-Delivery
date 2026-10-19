# Evidence Index — 15 Teacher Tasks

Status meanings: **Verified** = observed in a real run and recorded; **Blocked** = cannot be shown
yet, with the reason; **Pending** = not done yet. Nothing here is claimed without a file, log, PR,
image or command result behind it. Repository: https://github.com/Hussain-Anajwala/Automated-Last-Mile-Delivery

| # | Teacher task | Status | Evidence |
|---|---|---|---|
| 1 | Problem definition and scope | Verified | `docs/PROBLEM_SCOPE.md`, `docs/backlog.md` (exactly 15 tasks), `docs/PRD.md` |
| 2 | Agile planning and DevOps workflow | Verified | `docs/user-stories.md`, `docs/project-board.md` (board, milestones, DoD), `docs/GIT_WORKFLOW.md`, `docs/DEVOPS_WORKFLOW.md` (Mermaid) |
| 3 | Requirements, architecture, technology setup | Verified except target gates | `docs/ARCHITECTURE.md`, `docs/VERSIONS.md`, `phase-03-setup/compatibility-gate.md` (G1–G8 pass; G9 SSH to target blocked — B4) |
| 4 | Git/GitHub initialization | Verified | `phase-04-repo/repository-init.md` (fresh-clone build, first push), `phase-04-repo/secret-gate-verification.md`, issues #1–#7, `.github/` templates |
| 5 | First feature with branching and PR | Verified (self-review only) | PR #8 (merged `0820c84`), `phase-05-feature/` (36 tests, live smoke, review finding + regression test). Independent reviewer: not provided — see teacher question Q2 |
| 6 | MVP, collaboration, conflict, release tag | Verified; tag see below | PR #9 (merged `94756f8`, 88 tests, review findings fixed), `phase-06-mvp/acceptance.md` (A1–A7), `merge-conflict-transcript.md` (`demo/conflict-*`), screenshots; release tag `v0.1.0` — see `docs/IMPLEMENTATION_LOG.md` |
| 7 | Jenkins CI job | Verified | `phase-07-jenkins/` — SCM-scan-triggered green builds, archived JAR, JUnit, deliberately failing build exit 1 |
| 8 | Pipeline as code and deployment | Verified (local URL) | `Jenkinsfile`, `phase-08-deploy/` — deploy behind Nginx, parameter evidence (#6 rejected, #7 port 8089), screenshots. URL is local: `http://127.0.0.1:8088` (Q1) |
| 9 | Selenium design and local run | Verified | `docs/SELENIUM_TEST_PLAN.md`, `phase-09-selenium/` — 5 journeys/8 tests, flake diagnosed and fixed, 5 consecutive green runs, defect caught with screenshot, HTML report |
| 10 | Continuous testing in Jenkins | Verified | `phase-10-quality-gate/` — #9 red at Selenium gate with nothing packaged/deployed, fix + regression test, #10 green |
| 11 | Docker image and lifecycle | Verified | `Dockerfile`, `docs/DOCKER.md`, `phase-11-docker/lifecycle-transcript.md` |
| 12 | Jenkins→Docker CD with local registry | Verified on the workstation | `phase-12-registry/` — digest match pushed vs registry, deploy by registry tag, fault-injected candidate rolled back automatically. Pull by the isolated target: blocked (B4) |
| 13 | Ansible configuration management | **Blocked (execution)** | Authored and statically verified: `ansible/`, `docs/ANSIBLE.md`, `phase-13-ansible/static-checks.md` (syntax, lint production profile, read-only template validation with negative control). First run on a managed host: not done (B4) |
| 14 | Provisioning, idempotency, health, rollback on the target | **Blocked** | Prepared: `rollback.yml`, `docs/ANSIBLE_TARGET_SETUP.md`. No target run, no second-run `changed=0`, no target rollback yet (B4). Pipeline-level rollback on the workstation is verified under task 12 |
| 15 | End-to-end release, documentation, viva | **In progress** | Docs: `docs/SECURITY.md`, `docs/TROUBLESHOOTING.md`, `docs/DEMO_RUNBOOK.md`, `docs/VIVA_QA.md`, `docs/PRESENTATION_OUTLINE.md`. Full end-to-end run including Ansible: pending B4 |

## Cross-cutting evidence

| Topic | Evidence |
|---|---|
| Secret handling | `.gitleaks.toml`, `scripts/secret-scan.sh`, `scripts/safe-push.sh`, `scripts/test-secret-gate.sh`; incident record in `docs/IMPLEMENTATION_LOG.md` (2026-10-19 03:32 IST) |
| Data persistence | Deployed database kept its 6 deliveries / 15 history rows (created 22:15 UTC) across many app redeploys; Jenkins job history survived container recreation (recorded in the implementation log) |
| Decisions | `docs/DECISIONS_LOG.md` (D1–D17) |
| Open items | `docs/BLOCKERS.md` |
