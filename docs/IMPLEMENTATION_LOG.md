# Implementation Log

Phase records follow the template in `MASTER_PROMPT_CLAUDE_CODE.md` §12.

---

```text
PHASE: 0 — Repository and environment audit (pre-Phase 1)
STATUS: PASS (audit complete; readiness READY WITH BLOCKERS)
OBJECTIVE / SCOPE: Inspect repository, Git state, tools, Docker, WSL, ports, network and
  GitHub access before any change, as required by MASTER_PROMPT §2.
FILES CHANGED: docs/IMPLEMENTATION_LOG.md, docs/DECISIONS_LOG.md, docs/BLOCKERS.md,
  docs/evidence/README.md, docs/evidence/phase-00-audit/environment-audit.md (all new)
DECISIONS MADE AND SOURCES: D1–D5 in docs/DECISIONS_LOG.md
COMMANDS EXECUTED WITH EXIT CODES: see docs/evidence/phase-00-audit/environment-audit.md;
  all inspection commands exited 0 except tools reported "command not found"
  (mvn, gh, ansible, claude CLI) and docker server connection failure.
TESTS AND EXACT RESULTS: Not applicable (no code).
EVIDENCE PATHS / REAL URLS: docs/evidence/phase-00-audit/environment-audit.md
GIT BRANCH / COMMITS / PR / TAG: branch docs/environment-audit (local); no push yet
KNOWN ISSUES / SECURITY NOTES: Remote repository is public — every push needs a secret scan.
USER ACTIONS REQUIRED: B2–B6 in docs/BLOCKERS.md
EXIT CRITERIA:
  - Audit of repo/Git/tools/Docker/ports/GitHub/target performed: PASS
  - Living logs created: PASS
  - Readiness verdict recorded: PASS (READY WITH BLOCKERS)
AUTO-ADVANCE: YES — Phase 1 is documentation-only and depends on none of the blockers.
```

```text
PHASE: 1 — Problem definition and scope
STATUS: PASS
OBJECTIVE / SCOPE: Problem statement, stakeholders, objectives, constraints, assumptions,
  in/out scope, measurable acceptance criteria and a 15-task backlog. No application code.
FILES CHANGED: docs/PROBLEM_SCOPE.md (new), docs/backlog.md (new), docs/DECISIONS_LOG.md (D6–D8),
  docs/BLOCKERS.md (B1 resolved, B5 updated), docs/IMPLEMENTATION_LOG.md
DECISIONS MADE AND SOURCES: D6 ADMIN uses normal state machine (PRD §8, Schema §7);
  D7 assignment audit via history rows (App_Flow §4); D8 failure_reason cleared on retry (Schema §4)
COMMANDS EXECUTED WITH EXIT CODES: Adoptium API query (0); curl JDK ZIP + .sha256.txt (0);
  sha256sum match (0); Expand-Archive + java -version (0, 21.0.12.1 LTS);
  winget install GitHub.cli --scope user (0); gh --version (0, 2.102.0); gh auth status (1, not logged in)
TESTS AND EXACT RESULTS: Not applicable (documentation phase).
EVIDENCE PATHS / REAL URLS: docs/PROBLEM_SCOPE.md, docs/backlog.md
GIT BRANCH / COMMITS / PR / TAG: docs/phase-01-scope (local, merged to main with --no-ff)
KNOWN ISSUES / SECURITY NOTES: Assumptions AS1–AS5 await teacher confirmation (B6).
USER ACTIONS REQUIRED: B2–B6
EXIT CRITERIA:
  - Stakeholders, users, pain points, assumptions documented: PASS (PROBLEM_SCOPE §1–3, §9)
  - Roles and visibility confirmed: PASS (§4)
  - In/out scope and status rules confirmed: PASS (§5–6)
  - Measurable acceptance targets: PASS (§7, A1–A15); teacher wording confirmation pending (B6)
  - Exactly 15 backlog items with acceptance criteria: PASS (docs/backlog.md T1–T15)
  - No architecture-changing ambiguity; no app skeleton created: PASS (§10)
AUTO-ADVANCE: YES
```

```text
PHASE: 2 — Agile planning and DevOps workflow
STATUS: PASS
OBJECTIVE / SCOPE: User stories, Kanban board with dependency-aware sub-tickets, milestone plan
  (no invented dates), Definition of Done, Git/PR policy, Mermaid dev-to-ops workflow, evidence map.
FILES CHANGED: docs/user-stories.md, docs/project-board.md, docs/GIT_WORKFLOW.md,
  docs/DEVOPS_WORKFLOW.md (all new); docs/IMPLEMENTATION_LOG.md
DECISIONS MADE AND SOURCES: Board kept in Git until gh is authenticated (B5); merge commits
  (no squash) to preserve branch evidence; conflict demo on disposable demo/* branches (Implementation_Plan §5 Phase 6).
COMMANDS EXECUTED WITH EXIT CODES: grep ticket count per task (0): every T1–T15 has >=1 sub-ticket.
TESTS AND EXACT RESULTS: Not applicable (documentation phase).
EVIDENCE PATHS / REAL URLS: docs/project-board.md, docs/user-stories.md, docs/DEVOPS_WORKFLOW.md
GIT BRANCH / COMMITS / PR / TAG: docs/phase-02-agile (local, merged to main --no-ff)
KNOWN ISSUES / SECURITY NOTES: GitHub Issues mirror pending B5. Sprint dates not supplied by user.
USER ACTIONS REQUIRED: B2–B6 (optional: supply milestone dates)
EXIT CRITERIA:
  - Each of 15 tasks maps to phase, tickets, acceptance criteria, evidence: PASS (board + backlog)
  - User stories with testable criteria: PASS (US1–US10, DS1–DS9)
  - Board states Backlog/Ready/In Progress/Review/Blocked/Done: PASS
  - DoD and branch/PR policy: PASS
  - DevOps workflow diagram: PASS (Mermaid)
AUTO-ADVANCE: YES
```

```text
PHASE: 3 — Requirements, architecture and technology setup
STATUS: BLOCKED (partial) — software toolchain PASS; Docker/Jenkins/registry/Ansible gates blocked on B2–B4
OBJECTIVE / SCOPE: SRS summary, use-case and architecture diagrams, ERD reference, route/API list,
  verified and pinned versions, compatibility spike, single Jenkins and deployment topology, security review.
FILES CHANGED: docs/ARCHITECTURE.md, docs/VERSIONS.md, docs/evidence/phase-03-setup/compatibility-gate.md (new);
  docs/DECISIONS_LOG.md (D9–D12); docs/project-board.md; docs/IMPLEMENTATION_LOG.md
DECISIONS MADE AND SOURCES: D9 API auth; D10 Jenkins container; D11 embedded PostgreSQL tests; D12 continue 4–6.
  Version sources listed in docs/VERSIONS.md.
COMMANDS EXECUTED WITH EXIT CODES: Maven Central/Spring API/Docker Hub/PyPI/Jenkins update-center queries (0);
  wrapper SHA-1 + Maven SHA-512 verification (match); spike `mvnw.cmd -B verify` (0); `mvnw.cmd -v` (0);
  port/memory check (0); `docker version` (server unreachable).
TESTS AND EXACT RESULTS: Spike: Tests run: 2, Failures: 0, Errors: 0, Skipped: 0; BUILD SUCCESS.
EVIDENCE PATHS / REAL URLS: docs/evidence/phase-03-setup/compatibility-gate.md
GIT BRANCH / COMMITS / PR / TAG: docs/phase-03-architecture (merged to main --no-ff)
KNOWN ISSUES / SECURITY NOTES: Docker socket mount for Jenkins is root-equivalent (local only). Low free RAM.
USER ACTIONS REQUIRED: B2 (Docker), B3 (Ubuntu WSL), B4 (target choice), B5 (gh auth), B6 (teacher)
EXIT CRITERIA:
  - SRS, use-case, architecture, data model, API list: PASS (docs/ARCHITECTURE.md)
  - Versions verified and pinned with sources: PASS (docs/VERSIONS.md)
  - JDK/Maven/Spring/Flyway/PostgreSQL/Selenium spike: PASS (G1–G4)
  - One Jenkins topology and one deployment topology selected: PASS (D10, ARCHITECTURE §6)
  - Docker/Compose, Jenkins-Docker, registry push/pull: BLOCKED (B2)
  - WSL2 Ansible + SSH target: BLOCKED (B3, B4)
  - Ports/disk/memory recorded: PASS
AUTO-ADVANCE: NO for infrastructure-dependent phases (7, 8, 10–14). YES for Phases 4–6 under D12
  (they depend only on G1–G4, and the user directed continued independent work).
```

```text
PHASE: 4 — Git/GitHub repository initialization
STATUS: PASS (GitHub Issues mirror pending B5)
OBJECTIVE / SCOPE: Maven skeleton + wrapper, .gitignore/.gitattributes/.env.example, issue/PR templates,
  CONTRIBUTING, README, setup guide, develop branch, first push after secret scan.
FILES CHANGED: pom.xml, mvnw, mvnw.cmd, .mvn/wrapper/maven-wrapper.properties, .gitattributes, .gitignore,
  .env.example, .github/*, CONTRIBUTING.md, README.md, docs/SETUP.md, scripts/use-jdk21.ps1,
  src/main/java/com/lastmile/{LastMileApplication,health/HealthController}.java,
  src/main/resources/application.properties, src/test/java/com/lastmile/{support,health}/*
DECISIONS MADE AND SOURCES: Base package com.lastmile; JAR name lastmile-delivery.jar; gitleaks 8.30.1
  (winget, hash verified) as secret scanner; phase branches pushed as history evidence.
COMMANDS EXECUTED WITH EXIT CODES: see docs/evidence/phase-04-repo/repository-init.md (all 0).
TESTS AND EXACT RESULTS: Tests run: 3, Failures: 0, Errors: 0, Skipped: 0 (HealthControllerTest x2
  against embedded PostgreSQL 17.11; HealthControllerUnitTest x1). Fresh-clone build exit 0.
EVIDENCE PATHS / REAL URLS: https://github.com/Hussain-Anajwala/Automated-Last-Mile-Delivery ;
  docs/evidence/phase-04-repo/repository-init.md
GIT BRANCH / COMMITS / PR / TAG: build/project-skeleton c84ea37 → main 1663370; develop created at 1663370
KNOWN ISSUES / SECURITY NOTES: Repository is public; commit author metadata includes the configured Git
  email. No Issues created yet (B5).
USER ACTIONS REQUIRED: B5 gh auth login (for Issues/PRs)
EXIT CRITERIA:
  - Fresh clone contains no secrets: PASS (gitleaks history/staged/tree: no leaks)
  - Initial skeleton builds with documented commands: PASS (fresh clone mvnw.cmd -B verify exit 0)
  - README, .gitignore, .gitattributes, issue templates, branch policy: PASS
  - Repository URL + commit history: PASS (pushed)
  - GitHub issues: BLOCKED (B5) — templates + Git board used meanwhile
AUTO-ADVANCE: YES (exit gate criteria pass; issue mirroring is tracked under B5)
```

```text
PHASE: 5 — First feature with branching (delivery creation)
STATUS: BLOCKED (PR/review/merge only) — implementation, tests and live run PASS
OBJECTIVE / SCOPE: V1 schema, login, delivery creation with validation/code generation/history,
  create form + detail page, focused tests, branch + push + PR.
FILES CHANGED: see commits ba96a34, aa9b1ab (pom.xml, V1__core_schema.sql, user/security/delivery/
  workflow/web packages, templates, static css/js, tests, DevApplication, scripts/smoke_create.py, docs)
DECISIONS MADE AND SOURCES: D13 stacked branches while PR blocked; codes LMD-<UTC yyyyMMdd>-<seq>
  from a PostgreSQL sequence; CHECK constraints mirror the state rules at DB level.
COMMANDS EXECUTED WITH EXIT CODES: mvnw.cmd -B verify (1 then 0 after assertion fix; 0 after review fix);
  LoginTest without/with fix (1 / 0); spring-boot:test-run DevApplication (ran, health 200);
  smoke_create.py (0); gitleaks staged (0); git push feature/delivery-create (0); gh auth status (1).
TESTS AND EXACT RESULTS: Tests run: 36, Failures: 0, Errors: 0, Skipped: 0. Live smoke: valid create 302
  -> detail 200 CREATED + 1 history row; invalid create 200 with errors and preserved input; agent 403 on
  form, 404 on unassigned delivery; wrong password -> /login?error.
EVIDENCE PATHS / REAL URLS: docs/evidence/phase-05-feature/{feature-evidence.md,pull-request.md,*.png};
  branch https://github.com/Hussain-Anajwala/Automated-Last-Mile-Delivery/tree/feature/delivery-create
GIT BRANCH / COMMITS / PR / TAG: feature/delivery-create ba96a34, aa9b1ab (pushed). PR: NOT OPENED (B5).
KNOWN ISSUES / SECURITY NOTES: No login rate limiting. Code date is UTC.
USER ACTIONS REQUIRED: B5 gh auth login; B6.2 whether a second-person review is required.
EXIT CRITERIA:
  - Fresh local run creates a valid record and rejects invalid input: PASS (live smoke)
  - Tests for valid creation, invalid fields, generated/unique code: PASS
  - Issue + feature branch + push: branch PASS; GitHub issue BLOCKED (B5)
  - PR, review, merge: BLOCKED (B5); self-review finding fixed with regression test
AUTO-ADVANCE: YES for Phase 6 implementation under D12/D13 (stacked); Phase 5 remains BLOCKED until
  the PR is opened, reviewed as required and merged.
```

```text
PHASE: 6 — MVP completion and Git collaboration
STATUS: BLOCKED (release tag + PR merge only) — MVP features, tests, UI review and conflict demo PASS
OBJECTIVE / SCOPE: list/search/filter/pagination, edit, assignment/reassignment/retry, status workflow with
  atomic history, role-scoped dashboard, admin user list, JSON API, demo data, UI review, conflict demo, tag.
FILES CHANGED: commits dde4829, 8226f95, 9fa37b0 and this docs commit — delivery/dashboard/api/user/web/security
  packages, templates (list, form edit mode, detail actions, dashboard, admin/users), css, tests (7 new classes),
  scripts/smoke_api.sh, docs/API.md, docs/KNOWN_LIMITATIONS.md, evidence.
DECISIONS MADE AND SOURCES: D9 revised (stateless API; security flaw found in own design), D14 version token,
  DeliveryPolicy as single rule source for checks and visible controls.
COMMANDS EXECUTED WITH EXIT CODES: mvnw.cmd -B verify (0: 82 → 84 → 85 → 86 tests as tests were added);
  DevApplication live run (health 200); scripts/smoke_api.sh (all expected codes); Selenium screenshot capture
  (0 after harness click fix); gitleaks staged (0, no leaks); git push feature/delivery-workflow, demo/* (0).
TESTS AND EXACT RESULTS: Tests run: 86, Failures: 0, Errors: 0, Skipped: 0. Acceptance map A1–A7:
  docs/evidence/phase-06-mvp/acceptance.md.
EVIDENCE PATHS / REAL URLS: docs/evidence/phase-06-mvp/{acceptance.md,api-smoke-output.txt,
  merge-conflict-transcript.md,screenshots/*.png}; branches feature/delivery-workflow, demo/conflict-{base,a,b}
GIT BRANCH / COMMITS / PR / TAG: feature/delivery-workflow (stacked on feature/delivery-create); conflict
  resolution commit cbe5031 on demo/conflict-base; PRs not opened (B5); tag v0.1.0 NOT created (needs main).
KNOWN ISSUES / SECURITY NOTES: docs/KNOWN_LIMITATIONS.md (no login rate limiting, Basic auth API, UTC display).
USER ACTIONS REQUIRED: B5 (gh auth login) to open PRs #1 (delivery-create) and #2 (delivery-workflow), merge
  to develop → main, then tag v0.1.0; B6.2 independent reviewer if required.
EXIT CRITERIA:
  - PRD acceptance targets A1–A7 pass: PASS (tests + live smoke)
  - Application usable locally: PASS (DevApplication + screenshots desktop/mobile)
  - Service/repository/security tests incl. denied access, terminal status, invalid transition, no-result
    search, dashboard correctness: PASS
  - Second feature branch + isolated conflict created and resolved: PASS (transcript)
  - Release-ready baseline tagged: BLOCKED (B5 — PR merge to main required first)
AUTO-ADVANCE: YES for independent work (Phase 9 Selenium depends only on T6) under D12. Phases 7, 8, 10–14
  remain blocked on Docker (B2). Phase 6 stays BLOCKED until PRs merge and v0.1.0 is tagged.
```

```text
PHASE: 9 — Selenium test design and local execution
STATUS: PASS
OBJECTIVE / SCOPE: Test plan, 5 journeys (8 tests) with data-testid locators, explicit waits, isolated data,
  failure screenshots/HTML/URL, JUnit XML + HTML report, local execution through Maven.
  (Run before Phases 7–8, which are blocked on Docker; Phase 9 depends only on T6 — D12.)
FILES CHANGED: pom.xml (selenium-java test dep, `selenium` profile: failsafe + surefire-report 3.5.6),
  src/test/java/com/lastmile/selenium/* (BrowserFactory, BrowserJourney, FailureArtifactsExtension, J1–J5),
  docs/SELENIUM_TEST_PLAN.md, docs/evidence/phase-09-selenium/*
DECISIONS MADE AND SOURCES: Boot-managed Selenium 4.43.0; journeys in Failsafe behind a profile so the default
  build stays browser-free; window-marker page-load wait (after diagnosing a real flaky run).
COMMANDS EXECUTED WITH EXIT CODES: see docs/evidence/phase-09-selenium/selenium-evidence.md
  (first run 0; repeat 3 = 1 diagnosed; 5 consecutive = 0; deliberate defect = 1; final clean = 0).
TESTS AND EXACT RESULTS: Tests run: 86 (unit/integration) + 8 (Selenium), Failures: 0, Errors: 0, Skipped: 0.
EVIDENCE PATHS / REAL URLS: docs/evidence/phase-09-selenium/{selenium-evidence.md,passing-report/,defect-caught/,
  flaky-run-diagnosed/}
GIT BRANCH / COMMITS / PR / TAG: test/selenium-journeys (pushed); PR blocked (B5)
KNOWN ISSUES / SECURITY NOTES: Jenkins/container execution not yet proven (Phase 10, blocked on B2).
USER ACTIONS REQUIRED: none for this phase
EXIT CRITERIA:
  - Test plan + 3–5 critical journeys: PASS (5 journeys, 8 tests)
  - Stable selectors, deterministic data, explicit waits, real assertions: PASS
  - Runs from documented command, repeatable: PASS (5 consecutive green runs after fixing diagnosed flake)
  - Catches a known UI/workflow defect: PASS (deliberate defect → J5 failed)
  - Failure screenshot mechanism demonstrated: PASS (defect-caught/*.png, flaky-run-diagnosed/*.png)
  - Report published locally: PASS (target/reports/failsafe.html; JUnit XML)
AUTO-ADVANCE: Phase 10 requires Jenkins (B2) → BLOCKED. Continue with independent Phase 11/12/13 preparation
  (Dockerfile, Compose, Jenkinsfile, Ansible files) that can be written and statically checked without Docker.
```

```text
PHASE: 3 — (update) infrastructure gates re-checked after user set up Docker and Ubuntu
STATUS: BLOCKED (partial) — G5, G6, G8 now PASS; G7 pending Phase 12; G9 blocked on B4 (target choice)
EVIDENCE: docs/evidence/phase-03-setup/compatibility-gate.md (updated rows G5–G9)

PHASE: 7 — Jenkins installation and continuous integration job
STATUS: PASS
OBJECTIVE / SCOPE: Jenkins in the D10 topology, configured as code; repository connected; SCM-polling trigger;
  build + unit/integration tests; archived JAR and JUnit results; deliberate red build.
FILES CHANGED: ci/jenkins/{Dockerfile,plugins.txt,casc.yaml,compose.yaml}, Jenkinsfile,
  scripts/jenkins-init-env.ps1, docs/CI_JENKINS.md, docs/BLOCKERS.md (B2/B3 resolved, B7 added), mvnw (+x)
DECISIONS MADE AND SOURCES: multibranch job with 2-minute periodic scan as the SCM-polling trigger (no webhook,
  Jenkins is localhost-only); plugin pins from the 2.580.1 update center.
COMMANDS EXECUTED WITH EXIT CODES: docker compose build (1, 1 — WSL VM restarts; 0 on third attempt);
  docker compose up -d (0); curl checks 403/200; builds via branch indexing (see evidence).
TESTS AND EXACT RESULTS: Jenkins ci/jenkins-pipeline #1 SUCCESS 86/86; ci/demo-failing-build #1 FAILURE
  (85 passed, 1 failed, exit code 1); ci/jenkins-pipeline #2 SUCCESS 86/86.
EVIDENCE PATHS / REAL URLS: docs/evidence/phase-07-jenkins/jenkins-evidence.md + logs; Jenkins UI at
  http://127.0.0.1:8090 (local only, not a public URL)
GIT BRANCH / COMMITS / PR / TAG: ci/jenkins-pipeline bf988b0, d21fe6c, 99f1369 (pushed); ci/demo-failing-build dd773f6
KNOWN ISSUES / SECURITY NOTES: Docker socket = root-equivalent (local only). Commit-limit exhaustion (B7).
  Built-in node executes builds (no separate agent) — acceptable for a single-user local CI.
USER ACTIONS REQUIRED: B7 (memory) recommended; B5 for PRs.
EXIT CRITERIA:
  - Jenkins installed in the selected topology with reproducible config: PASS
  - Known commit detected by Jenkins and builds successfully, repeatably: PASS (#1 d21fe6c, #2 99f1369)
  - Build + unit/integration tests + package; artifact and reports archived: PASS
  - Deliberately failing build exits non-zero: PASS (exit code 1, FAILURE, no artifact)
  - No credentials in console logs: PASS (checked)
AUTO-ADVANCE: YES → Phase 8
```

```text
INCIDENT 2026-10-19 03:32 IST — secret-scan step did not gate a push
WHAT HAPPENED: Commit 9ee4a53 was committed and pushed although gitleaks reported "leaks found: 23". The command
  piped gitleaks into `tail`, so the pipeline returned tail's exit status (0) and the `&&` chain continued.
IMPACT: None — all 23 findings were reviewed: public Jenkins plugin identifiers in
  docs/evidence/phase-07-jenkins/installed-plugins.txt (form `<name>-api:<version>.v<build-hash>`) matching the
  generic-api-key rule. No credential was committed or pushed. Full-history scan with the reviewed allowlist: 0 findings.
FIX: .gitleaks.toml (default rules + one-path allowlist for that file); scripts/secret-scan.sh returns gitleaks'
  exit status and is used as `scripts/secret-scan.sh staged && git commit …` without pipes. Verified: planted
  random key → staged exit 1, tree exit 1; removed → exit 0. Tree mode ignores only git-ignored files
  (ci/jenkins/.env legitimately holds the local Jenkins password).
```

```text
PHASE: 8 — Pipeline as code and server deployment
STATUS: PASS
OBJECTIVE / SCOPE: Jenkinsfile with checkout, parameter validation, build/test, image packaging, deployment and
  verification; ≥1 parameter; Nginx reverse proxy; runtime secrets; local deployed URL.
FILES CHANGED: Jenkinsfile, Dockerfile, .dockerignore, deploy/compose.yaml, deploy/nginx/{Dockerfile,default.conf},
  ci/jenkins/casc.yaml (credentials), scripts/jenkins-init-env.ps1, docs/DEPLOYMENT.md, docs/CI_JENKINS.md
DECISIONS MADE AND SOURCES: Images built from the tested JAR (build once, promote); Nginx config baked into an
  image (Docker-outside-of-Docker bind mounts resolve on the host); deploy only on listed branches; verification
  from inside the Nginx container because the published port is bound to the Windows host loopback.
COMMANDS EXECUTED WITH EXIT CODES: local image build (0); compose config checks (0; guards fail as designed);
  Jenkins recreate with credentials (0); builds #5 SUCCESS, #6 FAILURE (intended), #7 SUCCESS.
TESTS AND EXACT RESULTS: build #5: 86/86, all three containers healthy, readiness UP via Nginx, deployed
  revision == commit 3af9df95d0b1. Host: readiness 200, login 200, redirect keeps port 8088.
EVIDENCE PATHS / REAL URLS: docs/evidence/phase-08-deploy/ (logs, screenshots, deploy-evidence.md);
  local URL http://127.0.0.1:8088 (not public)
GIT BRANCH / COMMITS / PR / TAG: ci/jenkins-pipeline 3af9df9 (pushed)
KNOWN ISSUES / SECURITY NOTES: deployment target is Docker Desktop on the workstation; the isolated Ubuntu
  target comes in Phases 13–14. Rollback automation comes in Phases 12/14.
USER ACTIONS REQUIRED: none for this phase
EXIT CRITERIA:
  - Jenkinsfile with checkout/build/test/package/deploy/health stages: PASS
  - ≥1 parameterized environment setting with validation: PASS (HTTP_PORT, DEPLOY_PROFILE, DEPLOY; #6/#7)
  - Deployed behind Nginx, app port private, secrets at runtime: PASS
  - Health/readiness verified and deployed version captured: PASS
  - Failed build/tests do not deploy: PASS (#6 skipped all later stages, running app unchanged);
    test-failure-specific proof follows in Phase 10 (Selenium gate)
AUTO-ADVANCE: YES → Phase 10 (Phase 9 already PASS)
```

```text
PHASE: 10 — Continuous testing in Jenkins
STATUS: PASS
OBJECTIVE / SCOPE: Selenium stage in the pipeline with published reports; failed Selenium blocks deployment;
  deliberate defect → red; fix with regression test → green.
FILES CHANGED: Jenkinsfile (Selenium quality gate stage), templates/deliveries/detail.html (defect + fix),
  WebAuthorizationTest (regression), docs/CI_JENKINS.md, evidence
DECISIONS MADE AND SOURCES: --no-sandbox for Chromium inside the unprivileged Jenkins container (browser visits
  only the local test app); defect chosen to be invisible to MockMvc so only the browser gate can catch it.
COMMANDS EXECUTED WITH EXIT CODES: Jenkins builds #8 SUCCESS, #9 FAILURE (intended), #10 SUCCESS;
  local regression test with defect exit 1, after fix mvnw verify exit 0 (87 tests).
TESTS AND EXACT RESULTS: see docs/evidence/phase-10-quality-gate/quality-gate-evidence.md
EVIDENCE PATHS / REAL URLS: docs/evidence/phase-10-quality-gate/
GIT BRANCH / COMMITS / PR / TAG: ci/jenkins-pipeline 62d40a3, 7f3a6e4 (defect), eb87044 (fix)
KNOWN ISSUES / SECURITY NOTES: Jenkins HTML publisher applies its default CSP, so the report renders unstyled.
USER ACTIONS REQUIRED: none
EXIT CRITERIA:
  - Selenium suite runs in Jenkins with published reports: PASS (#8, #10)
  - Reports/screenshots archived even on failure: PASS (#9 artifacts)
  - Failed Selenium test stops deployment: PASS (#9 skipped package/deploy; deployment unchanged)
  - Deliberate defect, failed gate, fix with regression test, green rerun: PASS
  - Defect removed before release: PASS (eb87044)
AUTO-ADVANCE: YES → Phase 11
```

```text
PHASE: 11 — Docker image and container lifecycle
STATUS: PASS
OBJECTIVE / SCOPE: Secure multi-stage Dockerfile, non-root, healthcheck, .dockerignore, runtime configuration,
  build/tag/run/inspect/logs/stop/start/restart/remove with a real transcript.
FILES CHANGED: Dockerfile, .dockerignore (Phase 8 commit 3af9df9), scripts/docker-lifecycle-demo.sh, docs/DOCKER.md,
  docs/evidence/phase-11-docker/lifecycle-transcript.md
DECISIONS MADE AND SOURCES: package the tested JAR (build once, promote); layered extraction; JRE Alpine base;
  readiness-based HEALTHCHECK (TRD §2: no misleading "healthy").
COMMANDS EXECUTED WITH EXIT CODES: bash scripts/docker-lifecycle-demo.sh → exit 0 (all steps in transcript).
TESTS AND EXACT RESULTS: container healthy after run/start/restart; readiness 200; with DB stopped readiness 503
  and liveness 200; inspect: user 10001:10001, read-only rootfs, CapDrop ALL, port 127.0.0.1:18081, revision label
  = commit; image 435da907d9bc, 405MB on disk; demo resources removed, deployed stack untouched.
EVIDENCE PATHS / REAL URLS: docs/evidence/phase-11-docker/lifecycle-transcript.md
GIT BRANCH / COMMITS / PR / TAG: ci/jenkins-pipeline (this commit)
KNOWN ISSUES / SECURITY NOTES: image size dominated by the JRE base (~212MB); jlink-trimmed runtime is future work.
USER ACTIONS REQUIRED: none
EXIT CRITERIA:
  - Secure, maintainable Dockerfile (multi-stage, non-root, healthcheck, .dockerignore): PASS
  - Traceable tag; runtime config, no secrets in layers: PASS
  - Build/tag/run/inspect/logs/stop/start/restart/remove demonstrated: PASS
  - Clean image starts and reaches readiness with documented configuration: PASS
AUTO-ADVANCE: YES → Phase 12
```

```text
PHASE: 12 — Jenkins-Docker continuous deployment with a local registry
STATUS: PASS for the workstation deployment target; target-node pull pending Phase 14 (B4)
OBJECTIVE / SCOPE: versioned images pushed to a local registry only after all gates, digest-verified, deployed by
  explicit tag pulled from the registry, last-known-good recorded, automatic rollback on unhealthy candidates.
FILES CHANGED: infra/registry/compose.yaml, Jenkinsfile (publish + deploy + rollback + FAULT_INJECTION),
  deploy/compose.yaml (LASTMILE_DB_HOST), ci/jenkins/compose.yaml (registry network), docs/REGISTRY.md, evidence
DECISIONS MADE AND SOURCES: identity by digest because registry names differ per namespace (risk R2);
  rollback restores the release that was running before the deploy (immutable tag in the registry).
COMMANDS EXECUTED WITH EXIT CODES: registry up (0); daemon push to localhost:5000 (0); Jenkins builds #13 SUCCESS,
  #14 ABORTED (deliberate), #15 FAILURE with ROLLBACK SUCCEEDED (deliberate drill).
TESTS AND EXACT RESULTS: see docs/evidence/phase-12-registry/registry-evidence.md
EVIDENCE PATHS / REAL URLS: docs/evidence/phase-12-registry/ (registry served only on http://127.0.0.1:5000)
GIT BRANCH / COMMITS / PR / TAG: ci/jenkins-pipeline e13d79d, 881a67f
KNOWN ISSUES / SECURITY NOTES: registry has no auth/TLS — loopback only. Rollback restores the image and its
  configuration but not database contents (forward-only migrations; see Phase 14 notes).
USER ACTIONS REQUIRED: B4 (target) for the target-side pull
EXIT CRITERIA:
  - Versioned image built and pushed only after mandatory tests: PASS
  - Registry push + pull of the exact tag, digest verified: PASS (workstation daemon); target node: BLOCKED (B4)
  - Deploy by explicit immutable tag; last known good retained: PASS
  - Health check after deploy; failed candidate restored to previous release: PASS (#15)
  - Registry local/private, never publicly exposed: PASS (127.0.0.1:5000 only)
AUTO-ADVANCE: YES → Phase 13 authoring and static checks; execution against the target BLOCKED (B4)
```

```text
PHASE: 13 — Ansible configuration management
STATUS: BLOCKED (execution) — specification, inventory template, roles, syntax check and lint PASS
FILES CHANGED: ansible/{ansible.cfg,site.yml,group_vars,inventory/hosts.example.yml,roles/*}, scripts/ansible-run.sh,
  docs/ANSIBLE.md (configuration specification), docs/BLOCKERS.md (B4 options)
COMMANDS EXECUTED WITH EXIT CODES: ansible-playbook --syntax-check (0); ansible-lint 26.10.1 (Passed, production
  profile, after fixing 16 findings). No run against any host.
EXIT CRITERIA:
  - Configuration specification, inventory, variables, roles/templates/handlers: PASS
  - Syntax check: PASS; check mode and first real run on the isolated target: BLOCKED (B4)
AUTO-ADVANCE: NO — Phases 13 (execution) and 14 need the target (B4). Phase 15 can only be partially prepared.
```

```text
CHECKPOINT 2026-10-19 ~04:50 IST — where to resume
STATE:
  - Phases 1, 2, 4, 7, 8, 9, 10, 11 PASS. Phase 12 PASS (workstation target). Phase 3 partial (G7 target pull, G9 SSH).
  - Phase 5 and 6: implementation PASS; PRs, merges to develop/main and tag v0.1.0 BLOCKED on B5 (gh auth).
  - Phase 13: authored and statically checked; execution BLOCKED on B4. Phase 14 BLOCKED on B4. Phase 15 NOT STARTED.
BRANCH STACK (open PRs in this order once gh is authenticated; each into develop, then develop -> main):
  feature/delivery-create -> feature/delivery-workflow -> test/selenium-journeys -> ci/jenkins-pipeline
  -> infra/ansible-provisioning (current HEAD). Not for merging: ci/demo-failing-build, demo/conflict-*.
RUNNING LOCALLY: Jenkins http://127.0.0.1:8090 (ci/jenkins/compose.yaml), registry 127.0.0.1:5000
  (infra/registry/compose.yaml), app http://127.0.0.1:8088 = localhost:5000/lastmile-delivery:0ac1a53c836f (healthy).
NEXT ACTIONS:
  1. After `gh auth login`: open PRs #1..#5 with docs/evidence/phase-05-feature/pull-request.md as the first body;
     merge in order when checks are green (and a real reviewer has approved, if the teacher requires one);
     tag v0.1.0 on main after the MVP PRs merge.
  2. After the B4 decision: create/provision the target, `ansible -m ping`, `--check --diff`, run 1, run 2
     (expect changed=0), pull of the exact registry tag on the target, health, rollback to the previous tag.
  3. Phase 15: end-to-end run, final docs, evidence INDEX, viva material.
```

```text
SESSION 2026-10-19 (resume) — reconciliation
- Git: tree clean, branches in sync with origin; no tags, PRs or issues existed.
- GitHub auth (B5): logged in as Hussain-Anajwala (scopes repo, workflow) in Git Bash and PowerShell, same
  gh.exe. Earlier "not logged in" = stale session PATH + state before the user's login. B5 RESOLVED.
- Memory before work: commit free 6.2 GB; project containers ~1 GB.

SECRET GATE (issue #6, PR #13): two more fail-open paths found while preparing the test and fixed (scanner
crash in tree mode read as "no findings"; wrong GITLEAKS path silently fell back). scripts/test-secret-gate.sh:
10/10 checks in a throwaway repo; blocked pushes left the bare remote untouched. On older branches without the
gate the gated commit also failed closed (exit 127), confirming nothing bypasses it.

PHASE: 5 — First feature with branching
STATUS: PASS (self-review; independent review pending teacher answer Q2)
EVIDENCE: issue #1, PR #8 merged 0820c84; docs/evidence/phase-05-feature/, phase-06-mvp/pull-requests-and-release.md

PHASE: 6 — MVP completion and Git collaboration
STATUS: PASS (self-review; Q2 pending)
EVIDENCE: PR #9 merged 94756f8 with review fix 566904b (88 tests); conflict demo; PRs #10–#15; merged result tree
  c2f4ff68 tested locally (89 + 8) and identical on develop; Jenkins main #1 green and deployed; tag v0.1.0 →
  e3b9d31 (GitHub release). Remaining: independent review only if the teacher requires it.

AUDIT of Phases 7–12 (what real runs demonstrate vs. configuration only)
| Claim | Demonstrated by | Status |
| Jenkins detects changes and runs the right job | branch-indexing builds of ci/*, develop #1/#2, main #1 | demonstrated |
| Maven build + archived artifact | lastmile-delivery.jar archived on green builds | demonstrated |
| Selenium reports published; failed tests block deploy | ci/jenkins-pipeline #9 | demonstrated |
| Image version = source commit | revision label check in main #1 (e3b9d31) | demonstrated |
| Registry holds the image with matching digest | pushed vs Docker-Content-Digest in #13, develop, main #1 | demonstrated |
| Deployment uses the versioned image | `Deployed localhost:5000/lastmile-delivery:e3b9d31cae5b` | demonstrated |
| Health checks detect an unhealthy release | #15 `container lastmile-app-1 is unhealthy` | demonstrated |
| Recovery restores the previous image | #15 ROLLBACK SUCCEEDED (workstation); target: not yet (B4) | demonstrated / blocked |
| Jenkins data persists across rebuilds | job history intact after two container recreations | demonstrated |
| Database data persists across redeploys | 6 deliveries / 15 history rows from 22:15 UTC still present after redeploys | demonstrated |
| Only main deploys | develop #1/#2 `Stage "Deploy" skipped due to when conditional` | demonstrated |
| Configured only | HTML report renders unstyled (Jenkins CSP); no webhook (by design, SCM scan instead) | noted |

PHASE: 15 — End-to-end release, documentation and viva
STATUS: IN PROGRESS (not complete)
DONE: docs/evidence/INDEX.md, SECURITY.md, TROUBLESHOOTING.md, DEMO_RUNBOOK.md, VIVA_QA.md, PRESENTATION_OUTLINE.md;
  memory measured during the main pipeline (phase-15-release/memory-during-main-build.txt).
PENDING: full end-to-end run including Ansible on the managed target (B4); teacher answers Q1–Q5.
```

```text
CHECKPOINT 2026-10-19 ~07:45 IST
STATE: main = develop tree (e3b9d31, v0.1.0, deployed and healthy at http://127.0.0.1:8088). Phases 1, 2, 4–12 PASS;
  3 partial (target gates); 13–14 BLOCKED (B4); 15 in progress. Open issues #5 (Ansible), #7 (Phase 15).
  Phase 15 docs on branch docs/phase-15-release with PR open into develop (left open so a human reviewer can
  review it if the teacher requires — D15).
NEXT: on B4 approval follow docs/ANSIBLE_TARGET_SETUP.md (Option B steps 1–5), then ping, --check, run 1, run 2
  (changed=0), target pull of localhost:5000/<repo>:<sha>, health, rollback.yml; record in phase-13/14 evidence;
  then the end-to-end run for Phase 15 and tag v1.0.0.
```

```text
PHASE: 13 — Ansible configuration management
PHASE: 14 — Automated provisioning and reliability validation
STATUS: TECHNICAL ACCEPTANCE CRITERIA PASS on the user-approved WSL2 target `lastmile-target`;
        ACADEMIC ACCEPTANCE PENDING (the teacher has not confirmed WSL2 as the clean node — B6 Q3).
        Not marked fully complete until that is resolved.
SCOPE: Option B creation (GPG-verified Ubuntu rootfs, wsl --import, bootstrap), pre-checks, dry run, run 1,
  independent verification, run 2 idempotency, upgrade, rollback, failed-candidate drill.
FILES CHANGED (PR #17, merged eba80872): scripts/wsl-target/{bootstrap-target.sh,run-ansible.sh},
  ansible/inventory/wsl-target.yml, docker_engine (bip/address pools, check-mode tolerance), lastmile_app (db host,
  check-mode skips), site.yml/rollback.yml (boolean tag assertion), tests, docs, .gitignore (D19).
RESULTS (logs in docs/evidence/phase-13-ansible/):
  pre-checks: syntax 0, lint 0, validate 0 (ok=10 changed=0), negative control 2
  --check --diff: ok=21 changed=10 failed=0 (after fixing a real regex_search defect and check-mode handling); target unchanged
  ping: pong
  run 1 (0ac1a53c836f): ok=25 changed=13 failed=0; verification: packages; docker/containerd active+enabled; lastmile
    account not in docker group; /opt/lastmile 0750 root:lastmile; .env 0600 root:root; digests = registry digests;
    readiness via the target's Nginx 200; revision = tag
  run 2 (same): ok=24 changed=0 failed=0
  release B (e3b9d31cae5b): ok=25 changed=4 (env, stack, previous, current)
  rollback.yml: ok=21 changed=4 failed=0 -> 0ac1a53c836f healthy, readiness 200; DB rows from run 1 preserved
  failed candidate (bad DB host): failed=1, records unchanged; rollback.yml -e lastmile_rollback_record=current -> healthy
CI: Jenkins infra/ansible-wsl-target #1 aborted (scheduled while Jenkins was starting), #2 failed at git fetch
  (transient network right after the container started; ls-remote succeeded minutes later), #3 SUCCESS 89 + 8,
  deploy skipped.
OTHER: 15 Jenkins evidence logs from Phases 7–12 had never been committed (*.log ignored) — now tracked (D19).
  Memory: dropping the WSL page cache freed ~3 GB of host commit; Jenkins and the target were stopped when idle.
  Unchanged: Ubuntu-24.04 distribution, Docker Desktop data, PostgreSQL 16 service, Jenkins data, workstation release.
```

```text
CHECKPOINT 2026-10-19 — resume point
STATE: v0.1.0 (e3b9d31) on main, deployed by Jenkins on the workstation (127.0.0.1:8088) and by Ansible on the
  WSL2 target (127.0.0.1:8089 inside WSL), identical digests (docs/evidence/phase-15-release/e2e-trace-v0.1.0.md).
  Phases 1–12 PASS; 13–14 technically PASS, academic acceptance of the WSL2 target pending (Q3); 15 in progress.
  PR #16 (Phase 15 docs) open on purpose for a possible human review; issues #5 and #7 open.
WAITING ON: teacher answers Q1–Q5 (docs/BLOCKERS.md B6); optional .wslconfig decision (B7).
NEXT: when the teacher answers — record the answers; if a second reviewer is required, have PR #16 reviewed;
  merge PR #16, release v1.0.0 through develop -> main (Jenkins main deploy), deploy the same tag to the target,
  update docs/evidence/INDEX.md and close #5/#7. To use the target: keep `wsl -d lastmile-target -u root -- sleep
  infinity` running, then scripts/wsl-target/run-ansible.sh.
```

```text
FINAL — 2026-10-19
TEACHER RESPONSE (Q3, exact): "Do whatever you think is right and best for your system." -> WSL2 target kept (D20);
  recorded as discretion, not as a statement that WSL2 equals a VM; remaining uncertainty listed as a limitation.

PHASE: 13 — Ansible configuration management                       STATUS: PASS (WSL2 target, D20)
PHASE: 14 — Provisioning, idempotency, health, recovery            STATUS: PASS (WSL2 target, D20)
  Evidence re-checked from the committed logs: pong; run 1 changed=13; registry-identical digests; readiness 200;
  run 2 changed=0; rollback healthy; failed-candidate recovery; data preservation re-proved during the v1.0.0
  release with saved output (the earlier rollback data check had only been summarised).

PHASE: 15 — End-to-end release, documentation and viva              STATUS: PASS (technical deliverables)
  PR #16 merged fba69706 after self-review + Jenkins docs/phase-15-release #4 (89 + 8). Release PR #18 merged
  ff1cac19 (main tree = tested tree). Jenkins main #2 failed on transient DNS before checkout; main #3 passed
  (89 + 8), published, deployed. Ansible target: upgrade changed=4, second run changed=0, rollback to v0.1.0 and
  roll-forward healthy, digests identical, data preserved. Tag v1.0.0 -> ff1cac19 (annotated, gated push).
  Evidence: docs/evidence/phase-15-release/release-v1.0.0-evidence.md. The viva/presentation are delivered by
  the student (materials: VIVA_QA.md, PRESENTATION_OUTLINE.md, DEMO_RUNBOOK.md).

FINAL BRANCHES: main = v1.0.0 (ff1cac19, deployed on both targets). develop = main + post-release evidence.
  Release tags: v0.1.0 (e3b9d31), v1.0.0 (ff1cac19). Not for merging: ci/demo-failing-build, demo/conflict-*.
REVIEWS: all PRs (#8–#18) merged after documented self-review by the Claude Code assistant with green CI;
  no second human reviewer (D15).
```
