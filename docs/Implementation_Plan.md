# Implementation Plan and Claude Code Execution Contract

**Project:** Automated Last-Mile Delivery Management Platform  
**Version:** 1.1  
**Status:** Approved planning baseline; execution starts when the master prompt is launched in the intended repository  
**Planning approach:** 15 major phases aligned with the teacher's 15 tasks; smaller tickets live under each phase.

## 1. Planning principles

1. The teacher's 15 tasks are the controlling phase sequence. Do not combine phases in a way that loses required evidence.
2. This planning pack is the source of truth. If documents conflict, stop and report the conflict instead of guessing.
3. Keep the MVP limited to delivery CRUD, search/filter, roles, valid status workflow, history and dashboard. DevOps work surrounds this product; it is not an excuse to expand product scope.
4. Implement one phase at a time. At each phase boundary, collect actual evidence and apply the exit gate. Automatically advance when the gate passes; pause only for a real external blocker, a safety-sensitive action, or a decision that cannot be resolved from the approved documents and verified facts.
5. Never claim a test, deployment, conflict resolution, Ansible idempotency or rollback succeeded unless the corresponding command/output was observed.
6. Preserve a working baseline. Make small, scoped commits and avoid unrelated refactors in a phase.
7. No secrets, real customer data, private keys or default production passwords in Git or screenshots.
8. Use Windows PowerShell for Windows-specific commands and document when a command must run inside WSL2 Ubuntu or the Linux target.
9. Use a code-first UI workflow: implement and refine Thymeleaf/Bootstrap screens directly in source, then verify them in the running application and browser tests. No external design tool is required.
10. This document is the planning baseline, not evidence of implementation. The user authorizes execution by launching `MASTER_PROMPT_CLAUDE_CODE.md` in the intended repository. Until then, no code or infrastructure changes are implied.

## 2. Approved decisions and environment

### Product

- Three roles: `ADMIN`, `DISPATCHER`, `AGENT`.
- Delivery statuses: `CREATED`, `ASSIGNED`, `OUT_FOR_DELIVERY`, `DELIVERED`, `FAILED`.
- Delivery CRUD, search/filter, assignment, status history and role-scoped dashboard.
- Out of scope: GPS/maps, route optimization, integrations, notifications, payments and native mobile apps.

### Stack

- Java 21 LTS; compatible Spring Boot release.
- Spring MVC, Thymeleaf, Bootstrap, Spring Security, Spring Data JPA/Hibernate.
- PostgreSQL and Flyway migrations.
- Maven Wrapper, JUnit 5, Selenium WebDriver.
- Git/GitHub, Jenkins Pipeline-as-Code, Docker/Docker Compose, local Docker Registry, Nginx, Ansible.
- Code-first UI implementation and refinement; Claude Code for repository analysis, planning, implementation, verification and documentation.

### Local topology

- Developer workstation: Windows + PowerShell + Git + JDK 21 + Docker Desktop/WSL2.
- Prefer Docker Compose for application dependencies/runtime so the local setup is documented and repeatable.
- Jenkins may run natively on Windows or in a controlled container, but Phase 3 must test one concrete topology and freeze it. Avoid unsupported hybrid instructions.
- Ansible control environment: Ubuntu under WSL2.
- Ansible target: primary choice is an isolated local Ubuntu LTS VM reachable over SSH; fallback is a deliberately isolated Ubuntu WSL2 target distribution. Do not provision the everyday developer environment by accident.
- Local registry is not publicly exposed. Phase 3 must prove the exact image tag/reference can be pushed from Jenkins and pulled by the selected target.

## 3. Git and issue conventions

Suggested branches:

- `main`: reviewed, release-ready baseline.
- `develop`: integration branch if useful for the teacher's workflow.
- `feature/<short-name>`: feature work.
- `fix/<short-name>`: defect corrections.
- `docs/<short-name>`: documentation-only work.
- `release/<version>`: optional release stabilization.

Commit style: `<type>: <imperative summary>` such as `feat: add delivery creation validation`, `test: cover failed status transition`, `ci: publish Selenium results`, `docs: explain local registry setup`. Avoid vague messages such as `changes`, `final`, or `fix bug`.

Each issue/ticket should include: goal, user story or technical outcome, acceptance criteria, out-of-scope notes, test plan and evidence required. Pull requests should state the change, tests run, risks and screenshots/logs where applicable.

## 4. Definition of Done (DoD)

A ticket or phase is done when:

- Its acceptance criteria are met and visibly evidenced.
- Code follows existing package and naming conventions; no unexplained duplication or dead controls.
- Input validation, authorization and safe error handling are included where relevant.
- Focused tests pass; regression tests are added for fixed defects.
- Full required checks for that phase pass, or a blocker is explicitly documented and the phase is not falsely marked complete.
- Secret scan/review finds no credential leakage.
- README/docs/diagrams/config examples are updated to match actual behavior.
- Git diff is reviewed; no unrelated files or generated build output are committed.
- Changes are committed and pushed on a named branch and reviewed/merged as required.
- A handover note records changes, commands, test results, artifacts, known issues and the next gate.

## 5. The 15 implementation phases

### Phase 1 — Problem Definition and Scope

**Teacher deliverable:** problem statement, stakeholders, objectives, constraints and approved MVP scope.

Tasks:
- Review `PRD.md` and confirm stakeholders, target users, pain points and assumptions.
- Confirm the three roles and role-appropriate data visibility.
- Confirm included/excluded MVP scope and delivery status rules.
- Confirm proposed measurable acceptance targets with the subject teacher if rubric-specific wording exists.
- Create the initial 15-task backlog structure, without implementing code.

Evidence:
- Approved PRD and scope table.
- Stakeholder list and assumptions/constraints log.
- 15 major backlog items with acceptance criteria.

Exit gate: no feature-scope ambiguity that would alter the architecture or assignment evidence. Do not create the application skeleton in this phase.

### Phase 2 — Agile Planning and DevOps Workflow

**Teacher deliverable:** backlog, Kanban/Scrum plan, DoD and DevOps workflow diagram.

Tasks:
- Turn each major task into tickets and smaller dependencies.
- Write user stories and testable acceptance criteria for product features.
- Prepare a simple board with `Backlog`, `Ready`, `In Progress`, `Review`, `Blocked`, `Done` states.
- Define sprint/milestone boundaries suitable for available semester time; dates must be supplied/approved by the user rather than invented.
- Adopt the DoD above and write the DevOps lifecycle diagram.
- Add evidence requirements to every teacher task.

Evidence:
- Backlog/board export or screenshots, sprint plan and DoD.
- Development-to-operations workflow diagram.

Exit gate: each of the teacher's 15 tasks maps to a named phase, ticket(s), acceptance criteria and deliverable evidence.

### Phase 3 — Requirements, Architecture and Technology Setup

**Teacher deliverable:** SRS summary, use-case diagram, architecture diagram, data model/API list and working local setup.

Tasks:
- Review `TRD.md`, `App_Flow.md` and `Backend_Schema.md` for consistency.
- Draw a use-case diagram and finalize logical/deployment architecture.
- Verify JDK 21, Spring Boot compatibility, Maven Wrapper, PostgreSQL, Docker Desktop/Compose, Jenkins environment, Selenium browser and Ansible setup.
- Decide and document one Jenkins execution topology (native Windows or a trusted container); do not leave two competing paths.
- Verify the local registry address from the build/pipeline environment and managed target.
- Verify SSH and Ansible connectivity to the isolated Ubuntu target; verify idempotent harmless test tasks before application configuration.
- Check available ports, disk and memory. Record OS edition, Docker Desktop/WSL2 status and all required tool versions.
- Establish API routes, status transition rules, schema constraints and seed-data policy.
- Create the initial security and risks review; document unresolved decisions.

Evidence:
- SRS summary, use-case and architecture diagrams, data model/API catalogue.
- Local environment validation log and version table.
- A compatibility-gate checklist with pass/fail/blocker evidence.

Exit gate: clean environment checks pass; the selected Windows/Jenkins/Docker/registry/Ansible topology is reproducible. If it fails, resolve or document the smallest compatible adjustment before feature implementation.

### Phase 4 — Git and GitHub Repository Initialization

**Teacher deliverable:** repository URL, README, initial commits, issues and branch policy.

Tasks:
- Create the GitHub repository using a project-relevant name.
- Add the six approved documents under `docs/`, plus README, `.gitignore`, `.gitattributes`, issue templates and contribution/branch rules.
- Ensure LF endings for Docker/Jenkins/shell/Ansible files.
- Define required environment variables and include only placeholder values in `.env.example`.
- Initialize Maven Wrapper and a minimal Java/Spring Boot skeleton only after Phase 3 is approved.
- Commit an initial skeleton with meaningful messages.

Evidence:
- Repository URL and commit history.
- README setup overview, `.gitignore`, issue templates, branch convention.

Exit gate: a fresh clone contains no secrets and the initial skeleton builds using documented commands.

### Phase 5 — Feature Development with Branching

**Teacher deliverable:** first working feature, feature branch, pull request, review comments and merge evidence.

Recommended first feature: delivery creation with validation and generated delivery code.

Tasks:
- Create an issue with acceptance criteria; create `feature/delivery-create` (or consistent name).
- Implement the smallest vertical slice: migration/entity, repository, service validation, MVC form/controller and success/error UI.
- Add tests for valid creation, invalid required fields and duplicate/generated code constraints.
- Use `git add`, `commit`, `push`, `pull`, `log`; demonstrate actual commands with real output.
- Open PR, review the diff, address at least one meaningful review finding if one exists (do not manufacture misleading review evidence), merge into the agreed integration branch.

Evidence:
- Working feature and tests, branch history, PR link/screenshots, review and merge.

Exit gate: fresh local run can create a valid record and reject invalid input. Do not proceed with failing build/tests.

### Phase 6 — MVP Completion and Git Collaboration

**Teacher deliverable:** functional MVP, resolved merge conflict, tagged version and updated backlog.

Tasks:
- Add list/detail/edit/search/filter/pagination.
- Implement login/logout and the roles/ownership policy.
- Implement assignment, allowed status transitions, failure reasons and transactional history.
- Implement role-scoped dashboard counts.
- Add service/repository/security tests and deterministic demo fixtures.
- Add a second feature on a separate branch. Create a deliberate, small conflict in a documentation/config text file with two branches, resolve it correctly, and show final file plus history. Never manufacture a conflict in source-critical or binary/generated files.
- Review test coverage for denied access, terminal status, invalid transition, no-result search and dashboard correctness.
- Fix accessibility and UI issues using the approved code-first design-system tokens.
- Tag a reviewed release-ready baseline only after acceptance checks pass.

Evidence:
- Demonstration of all MVP features and role boundaries.
- Conflict creation/resolution evidence, merge history and release tag.
- Updated backlog and known-limitations list.

Exit gate: PRD acceptance targets pass; the application is usable locally and the source baseline is release-ready.

### Phase 7 — Jenkins Installation and Continuous Integration

**Teacher deliverable:** Jenkins job, successful build log, trigger evidence and archived artifact.

Tasks:
- Install/configure Jenkins in the Phase 3-selected topology.
- Connect the GitHub repository using a scoped credential; use webhooks if reachable, otherwise document a supported polling/scheduled trigger.
- Configure required plugins and ensure Jenkins uses the agreed JDK/Maven Wrapper.
- Add a pipeline/job that checks out code, compiles, runs unit/integration tests and packages the application.
- Archive the artifact and publish build/test reports.
- Verify non-secret credentials are not exposed in console logs.

Evidence:
- Job configuration, trigger configuration, successful console output and archived artifact.

Exit gate: a known commit triggers or is detected by Jenkins and produces a repeatable successful build; a deliberately failing build exits nonzero.

### Phase 8 — Pipeline as Code and Server Deployment

**Teacher deliverable:** Jenkinsfile, successful pipeline run, deployed application URL/screenshot and configuration evidence.

Tasks:
- Create a declarative `Jenkinsfile` committed with the repository.
- Implement checkout, build, test, package and controlled deployment stages.
- Parameterize at least one environment setting, such as image tag or application profile; use safe defaults and validate input.
- Configure Nginx reverse proxy and application/database networking.
- Use runtime environment configuration for secrets and connection strings.
- Capture deployment version and run health/readiness checks.
- Document local URL and port mappings; do not assume an internet-facing URL if deployment is local.

Evidence:
- Jenkinsfile, pipeline-stage output, deployed-app screenshot, parameter evidence and health result.

Exit gate: a reviewed build can deploy reproducibly and is reachable via the documented local entry point. Failed build/tests must not deploy.

### Phase 9 — Selenium Test Design and Local Execution

**Teacher deliverable:** test plan, Selenium scripts, local test report and failure screenshot mechanism.

Tasks:
- Finalize 3–5 critical user journeys from `App_Flow.md`.
- Select stable locators (prefer explicit test IDs) and deterministic test data.
- Configure Selenium WebDriver/JUnit 5 through Maven; use headless browser mode where needed.
- Assert observable outcomes and authorization boundaries, not just page navigation.
- Capture screenshot, URL and useful page/test context on failure without leaking secrets.
- Run the tests locally and document setup/commands.

Evidence:
- Test plan, Selenium code, local execution log/report and a demonstrated failure screenshot mechanism.

Exit gate: test suite runs from the documented command, is repeatable and catches a known UI/workflow defect.

### Phase 10 — Continuous Testing in Jenkins

**Teacher deliverable:** Jenkins test report, failed pipeline evidence, defect correction commit and successful rerun.

Tasks:
- Integrate Selenium stage after build/unit/integration tests and start a dedicated test environment.
- Publish JUnit/Selenium results and archive failure screenshots even when the test stage fails.
- Configure the pipeline to stop deployment when a required test fails.
- Deliberately introduce one small defect on a temporary branch/commit (do not leave it in the release branch), run the pipeline and preserve failure evidence.
- Fix the defect with a regression test and meaningful commit, rerun pipeline and verify success.

Evidence:
- Failed Jenkins build with test report, defect issue/commit, corrected run and passing report.

Exit gate: the test stage is a real quality gate; a failed browser test cannot lead to deployment.

### Phase 11 — Docker Image and Container Lifecycle

**Teacher deliverable:** Dockerfile, image details, Docker command log and running container evidence.

Tasks:
- Add a secure and maintainable Dockerfile; multi-stage build if suitable.
- Build and tag the image with a traceable tag.
- Inject environment configuration at runtime; do not bake secrets into image layers.
- Run the container with required network/environment settings and health check.
- Demonstrate inspect, logs, stop, start/restart and removal. Keep durable database storage separate from app image.
- Document port mapping, service dependencies and image version inspection.

Evidence:
- Dockerfile, image ID/tags, running-container screenshot/logs and command transcript.

Exit gate: a clean image can start and reach its readiness state using documented configuration.

### Phase 12 — Jenkins-Docker Continuous Deployment

**Teacher deliverable:** versioned image, registry evidence and end-to-end commit-to-container pipeline.

Tasks:
- Add image build/tag/push stage after all mandatory tests pass.
- Use the local registry, with its hostname/port tested from Jenkins and the deployment target.
- Avoid `latest` as the sole release identifier; use a commit/build tag and retain the last known-good tag/digest.
- Deploy by explicit image tag; avoid mutable image references for rollback.
- Check deployment health and keep previous known-good release available.
- Demonstrate end-to-end change-to-container from a commit.

Evidence:
- Registry catalog/tag or equivalent pull evidence, image digest/tag, pipeline output and deployed-version proof.

Exit gate: Jenkins can publish and deploy a new version only after tests pass; registry is local/private, reachable and never publicly exposed.

### Phase 13 — Ansible Configuration Management

**Teacher deliverable:** configuration specification, Ansible inventory/playbook and first execution log.

Tasks:
- Document OS, required packages, deployment user/group, working directories, files, ports, services and permissions.
- Create inventory for the isolated Ubuntu target and variables for environment-specific settings.
- Write roles/tasks for necessary prerequisites and directory/service/config management.
- Use templates and handlers where appropriate; avoid passwords in source/console output.
- Validate syntax, run a check/diff mode where possible, then execute against the intended target.
- Document required privilege escalation and how SSH access is configured.

Evidence:
- Configuration specification, inventory/vars (without secrets), playbook/roles and first run output.

Exit gate: target is configured as described and the playbook can run again safely. Do not execute provisioning against an unintended host.

### Phase 14 — Automated Provisioning and Reliability Validation

**Teacher deliverable:** provisioned node, idempotency evidence, health-check result and rollback/recovery demonstration.

Tasks:
- Start from a clean or resettable target, apply Ansible and deploy the selected application image/container.
- Run the playbook a second time and show no unintended configuration changes; explain any expected changes.
- Execute health/readiness checks after deployment.
- Preserve a previously successful image and runtime config.
- Simulate a controlled unhealthy candidate or failed health check in an isolated environment, then restore the previous stable release.
- Verify recovered version and health; document database migration considerations.

Evidence:
- First and second Ansible runs, change summaries, target inventory, health output, failed candidate evidence and recovery evidence.

Exit gate: idempotency is demonstrated and rollback/recovery succeeds or a clearly bounded blocker and safe manual recovery are documented and approved. Never pretend a rollback succeeded when it did not.

### Phase 15 — End-to-End Release, Documentation and Viva

**Teacher deliverable:** final repository, live demo, complete report, screenshots/video, presentation and viva.

Tasks:
- Run the complete workflow from a clean/known commit through Jenkins build, unit/integration tests, Selenium quality gate, local registry publication, Docker deployment, Ansible provisioning/verification and health check.
- Verify branch rules and release tag point to the reviewed source baseline.
- Finalize README, PRD/TRD/design/flow/schema docs, architecture diagrams, setup guide, API catalogue, tests, deployment, troubleshooting, limitations and future enhancements.
- Capture redacted screenshots/logs/video showing application and required DevOps evidence.
- Prepare presentation: problem → solution → architecture → demonstration → CI/CD → container/provisioning → tests/recovery → limitations/future work.
- Prepare viva answers for design trade-offs, role enforcement, failed pipeline, local registry, idempotency, secrets, image tags and rollback limitations.

Evidence:
- Final tag/repository, clean setup instructions, successful end-to-end run, all assignment evidence, report and presentation.

Exit gate: every teacher deliverable maps to a verifiable file, log, PR, image, screenshot or demonstration step; known limitations are explicit.

## 6. Risk register and mitigations

| ID | Risk | Likelihood / impact | Mitigation / decision gate |
|---|---|---|---|
| R1 | Jenkins in Windows/container cannot access Docker daemon consistently | Medium / High | Phase 3 validates the exact selected topology using a test build/run. Prefer one documented Jenkins execution model; treat Docker socket/daemon access as privileged. |
| R2 | Local registry hostname works in one network namespace but not another | High / High | Prove push and pull from Jenkins and target before Phase 12; document one stable hostname/port and Docker Desktop host resolution. Do not expose registry publicly. |
| R3 | Ansible cannot manage Windows the same way as Linux | High / High | Use an isolated Ubuntu LTS VM reachable over SSH; isolated WSL2 distro is fallback. Verify SSH, privilege escalation and reset/recovery before Phase 13. |
| R4 | VM/WSL2/Docker Desktop consumes too much RAM/disk or virtualization is disabled | Medium / High | Phase 3 inventory; start with local profile and small images; use WSL2 fallback only if it remains isolated and can meet rubric requirements. Ask teacher before substituting target. |
| R5 | Selenium runs locally but fails in Jenkins due to browser/driver/display differences | High / Medium | Headless mode; pin browser/test setup or use a documented Selenium browser container; stable selectors and CI-like local run. |
| R6 | CI secrets appear in logs or source control | Medium / High | Jenkins credentials store, least-privilege credentials, `.gitignore`, secret review and redaction; no production values in `.env.example`. |
| R7 | Agent can access another agent's delivery or directly change status | Medium / High | Server-side role/ownership checks at service/API boundaries; tests for crafted URLs/requests and invalid transitions. |
| R8 | Status update commits without matching history | Low–Medium / High | Transactional status/history writes and integration tests for rollback on failure. |
| R9 | Database migration blocks old image rollback | Medium / High | Prefer backward-compatible additive migrations; record schema compatibility and recovery steps before destructive changes. |
| R10 | Phase 1–6 feature development expands before MVP is stable | Medium / Medium | Scope freeze, explicit out-of-scope list, phase gates and tickets tied to teacher tasks. |
| R11 | visual polish can outpace functional Thymeleaf implementation | Medium / Medium | Implement shared tokens/components in source and review the running screens with tests. |
| R12 | Tool/API version changes during semester | Medium / Medium | Pin versions and wrapper files at Phase 3; upgrade only as a reviewed issue with regression tests. |
| R13 | Port conflicts or Windows path/line-ending errors | Medium / Medium | Record a port map, check occupied ports during setup, use `.gitattributes`, and document PowerShell vs WSL commands. |
| R14 | Student/teacher expects public URL while only local deployment is planned | Medium / Medium | Confirm rubric interpretation in Phase 1/3; local URL/screenshots meet the plan only if accepted. If public hosting is required, scope and security review must be updated. |
| R15 | Flaky tests or test data leakage makes the pipeline unreliable | Medium / High | Deterministic data, isolated test environment, cleanup/unique IDs, bounded waits, reports and test quarantine only through explicit review. |

## 7. Decisions that must be confirmed before implementation

The broad architecture is decided, but the following operational details are Phase 3 gates, not hidden assumptions:

1. Which Jenkins execution topology works reliably on the Windows machine (native service or trusted container).
2. Whether a dedicated Ubuntu VM is available; if not, whether an isolated WSL2 target meets the teacher's definition of provisioning a clean node.
3. Exact local registry address reachable from Jenkins and the target.
4. Available RAM/disk/virtualization and free port map.
5. Exact pinned Java, Spring Boot, Maven plugin, Jenkins, Selenium/browser, Docker Compose and Ansible versions compatible with the environment.
6. Whether the teacher expects a publicly reachable application URL, external Tomcat, or accepts a Dockerized Spring Boot application behind Nginx.
7. Whether admin user-management CRUD is required; default scope is seeded accounts and role enforcement, with a minimal admin user list only if needed.

Do not begin core feature implementation until these high-impact choices are resolved or explicitly accepted as documented risks.

## 8. Phase reporting template

At every phase end, Claude Code should provide:

```text
PHASE: <number and name>
STATUS: PASS / BLOCKED / NOT COMPLETE
SCOPE IMPLEMENTED:
FILES CHANGED:
DECISIONS MADE (and source):
COMMANDS EXECUTED WITH EXIT CODES:
TESTS / CHECKS: <exact result, not just 'passed'>
EVIDENCE / ARTIFACTS:
GIT BRANCH / COMMITS / PR / TAG:
RISKS / KNOWN ISSUES:
DOCUMENTS UPDATED:
USER ACTIONS REQUIRED:
EXIT CRITERIA: <criterion-by-criterion PASS/FAIL/BLOCKED with evidence>
AUTO-ADVANCE: YES only if all required exit criteria pass; otherwise NO with exact reason
```

A phase marked `BLOCKED` or `NOT COMPLETE` does not authorize starting its dependent phase unless the user explicitly approves a documented exception.

## 9. Claude Code project instruction block

The full executable instruction is maintained in `MASTER_PROMPT_CLAUDE_CODE.md` at the repository root. Keep the root `CLAUDE.md` concise and use it for persistent repository rules; do not duplicate the complete master prompt into `CLAUDE.md`.

Claude Code must work phase-by-phase but should not ask for approval after every passing phase. It should automatically continue when the current phase's exit gate passes. It must pause for missing user credentials, system-level permission, destructive/external actions not clearly authorized, teacher-specific ambiguity that affects acceptance, or a failure that cannot be responsibly resolved. It should continue unrelated tasks while a dependency is blocked.

The approved stack is Java 21 LTS, compatible Spring Boot, Thymeleaf/Bootstrap, Spring Security, PostgreSQL, Flyway, Maven Wrapper, JUnit 5, Selenium, Jenkins Pipeline-as-Code, Docker/Compose, a local Docker registry, Nginx and Ansible controlled from Ubuntu under WSL2 on a Windows workstation. No Google Stitch or other external design tool is required. The managed provisioning target must be an isolated Ubuntu Linux node.

## 10. Final submission checklist

- [ ] All six planning documents reviewed and committed.
- [ ] One-to-one mapping from teacher's 15 tasks to project phases/evidence.
- [ ] Functional MVP and role-authorization tests.
- [ ] Git branch/PR/review/conflict/tag evidence.
- [ ] Jenkins CI job, trigger and archived artifact evidence.
- [ ] Jenkinsfile deployment and parameter evidence.
- [ ] Local Selenium tests, reports and failure screenshots.
- [ ] Failed CI gate, defect fix and successful rerun.
- [ ] Docker image/container lifecycle evidence.
- [ ] Local registry publication and commit-to-container deployment evidence.
- [ ] Ansible target setup and first execution evidence.
- [ ] Ansible idempotency, health-check and recovery/rollback evidence.
- [ ] README, diagrams, troubleshooting, limitations and future plan.
- [ ] Final report, redacted screenshot/video evidence and viva presentation.
