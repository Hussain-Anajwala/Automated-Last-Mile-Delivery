# Automated Last-Mile Delivery Management Platform

*LastMile Ops* — a semester DevOps project: a Spring Boot web application for recording, assigning,
searching and tracking last-mile deliveries, delivered through a Jenkins → Docker → local registry →
Ansible pipeline with Selenium quality gates and rollback.

> Learning/demo system with fictional data. Not a production logistics platform.

## Status

Release **v0.1.0** (MVP baseline) is tagged on `main` and was deployed by Jenkins after 89 unit/integration and
8 Selenium tests passed. Ansible execution on the managed target is still pending (see
[docs/BLOCKERS.md](docs/BLOCKERS.md) B4). Evidence for every teacher task: [docs/evidence/INDEX.md](docs/evidence/INDEX.md).
Progress log: [docs/IMPLEMENTATION_LOG.md](docs/IMPLEMENTATION_LOG.md). Nothing is marked complete
without recorded evidence ([docs/evidence/](docs/evidence/README.md)).

## Stack

Java 21 (Temurin) · Spring Boot 4.1.1 (MVC, Thymeleaf, Security, Data JPA) · PostgreSQL 17 · Flyway ·
Bootstrap 5 · Maven Wrapper · JUnit 6 · Selenium 4 · Jenkins Pipeline-as-Code · Docker/Compose ·
local Docker Registry · Nginx · Ansible. Exact versions and sources: [docs/VERSIONS.md](docs/VERSIONS.md).

## Quick start (tests and build)

```powershell
. .\scripts\use-jdk21.ps1
.\mvnw.cmd -B verify
```

Run the app locally without Docker (embedded PostgreSQL, fictional demo data):

```powershell
$env:LASTMILE_DEMO_PASSWORD = "<local value, at least 12 characters>"
$env:LASTMILE_SERVER_PORT = "18080"
.\mvnw.cmd spring-boot:test-run "-Dspring-boot.run.main-class=com.lastmile.DevApplication"
```

Details, Ubuntu/WSL commands and configuration: [docs/SETUP.md](docs/SETUP.md).

## Documentation

| Topic | Document |
|---|---|
| Problem, scope, acceptance criteria | [docs/PROBLEM_SCOPE.md](docs/PROBLEM_SCOPE.md), [docs/PRD.md](docs/PRD.md) |
| Backlog, board, user stories, DoD | [docs/backlog.md](docs/backlog.md), [docs/project-board.md](docs/project-board.md), [docs/user-stories.md](docs/user-stories.md) |
| Architecture, API, topologies | [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md), [docs/API.md](docs/API.md), [docs/TRD.md](docs/TRD.md) |
| Security notes | [docs/SECURITY.md](docs/SECURITY.md) |
| Troubleshooting | [docs/TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md) |
| Demo runbook, viva, presentation | [docs/DEMO_RUNBOOK.md](docs/DEMO_RUNBOOK.md), [docs/VIVA_QA.md](docs/VIVA_QA.md), [docs/PRESENTATION_OUTLINE.md](docs/PRESENTATION_OUTLINE.md) |
| Ansible | [docs/ANSIBLE.md](docs/ANSIBLE.md), [docs/ANSIBLE_TARGET_SETUP.md](docs/ANSIBLE_TARGET_SETUP.md) |
| Known limitations | [docs/KNOWN_LIMITATIONS.md](docs/KNOWN_LIMITATIONS.md) |
| Data model and API contracts | [docs/Backend_Schema.md](docs/Backend_Schema.md) |
| UI and application flow | [docs/UI_UX_Design.md](docs/UI_UX_Design.md), [docs/App_Flow.md](docs/App_Flow.md) |
| Selenium test plan | [docs/SELENIUM_TEST_PLAN.md](docs/SELENIUM_TEST_PLAN.md) |
| Docker images and lifecycle | [docs/DOCKER.md](docs/DOCKER.md) |
| Local registry and CD | [docs/REGISTRY.md](docs/REGISTRY.md) |
| Deployment | [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md) |
| Jenkins CI/CD | [docs/CI_JENKINS.md](docs/CI_JENKINS.md) |
| Git workflow | [docs/GIT_WORKFLOW.md](docs/GIT_WORKFLOW.md), [CONTRIBUTING.md](CONTRIBUTING.md) |
| DevOps workflow | [docs/DEVOPS_WORKFLOW.md](docs/DEVOPS_WORKFLOW.md) |
| 15-phase plan | [docs/Implementation_Plan.md](docs/Implementation_Plan.md) |
| Decisions and blockers | [docs/DECISIONS_LOG.md](docs/DECISIONS_LOG.md), [docs/BLOCKERS.md](docs/BLOCKERS.md) |

## Planning pack

The project started from an approved planning pack, kept in the repository as the source of truth:
the six documents in `docs/` (PRD, TRD, UI/UX design, App flow, Backend schema, Implementation plan),
[CLAUDE.md](CLAUDE.md) (persistent repository rules for Claude Code),
[MASTER_PROMPT_CLAUDE_CODE.md](MASTER_PROMPT_CLAUDE_CODE.md) (execution instruction) and
[USER_SETUP_CHECKLIST.md](USER_SETUP_CHECKLIST.md) (actions that need the developer's account,
machine privileges or decisions).

Key environment note: Windows is the workstation, not the Ansible target. The target is an isolated
Ubuntu node reachable over SSH. The local image registry stays off untrusted networks.
