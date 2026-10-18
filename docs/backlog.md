# Product and DevOps Backlog — 15 Top-Level Tasks

Exactly one top-level item per teacher task, in teacher order. Sub-tickets (`Tn.x`) are added in
Phase 2 (`docs/project-board.md`). Acceptance IDs `A1–A15` refer to `docs/PROBLEM_SCOPE.md` §7.

| # | Task | Acceptance criteria | Evidence | Depends on |
|---|---|---|---|---|
| T1 | Problem definition and scope | Problem, stakeholders, objectives, constraints, assumptions, in/out scope and measurable criteria documented; no architecture-changing ambiguity | `docs/PROBLEM_SCOPE.md`, this backlog | — |
| T2 | Agile planning and DevOps workflow | Every task has sub-tickets, user stories with testable criteria, board states, DoD, branch/PR policy, workflow diagram and evidence list | `docs/user-stories.md`, `docs/project-board.md`, `docs/GIT_WORKFLOW.md` | T1 |
| T3 | Requirements, architecture, technology setup | SRS summary, use-case and architecture diagrams, ERD, API list; pinned versions with sources; compatibility checklist with real pass/blocked results; one Jenkins topology and one deployment topology selected | `docs/ARCHITECTURE.md`, `docs/VERSIONS.md`, `docs/evidence/phase-03-*` | T2 |
| T4 | Git/GitHub repository initialization | Maven skeleton with wrapper builds (`mvnw verify` exit 0); `.gitignore`, `.gitattributes`, `.env.example`, issue templates; pushed to origin after clean secret scan | Repo URL, commit log, build log | T3 |
| T5 | First feature with branching | `feature/delivery-create` implements A1–A2 with tests; PR opened, diff reviewed, merged with passing checks | PR URL, test report, merge commit | T4 |
| T6 | MVP completion and collaboration | A3–A7 pass; security and transition tests; isolated merge conflict created and resolved; release tag on tested baseline | Test reports, conflict transcript, tag | T5 |
| T7 | Jenkins installation and CI job | Jenkins in selected topology; SCM polling detects a commit; build+tests pass; JAR and reports archived; a failing build exits non-zero (A9) | Jenkins console logs, artifact listing | T6 |
| T8 | Pipeline as code and deployment | `Jenkinsfile` with checkout/build/test/package/deploy/health stages; ≥1 parameter; app reachable through Nginx; failed tests do not deploy | Jenkinsfile, stage log, health output, screenshot | T7 |
| T9 | Selenium design and local run | Test plan; 3–5 journeys with `data-testid` selectors, explicit waits, assertions; screenshot on failure; report generated locally | Test plan, report, failure screenshot | T6 |
| T10 | Continuous testing in Jenkins | Selenium stage in pipeline; reports published even on failure; deliberate defect fails gate; fix + regression test; green rerun (A10) | Failed + passing Jenkins runs, fix commit | T8, T9 |
| T11 | Docker image and lifecycle | Multi-stage, non-root Dockerfile with healthcheck; build/tag/run/inspect/logs/stop/restart/rm demonstrated (A11) | Dockerfile, command transcript, image ID | T6 |
| T12 | Jenkins-Docker CD with local registry | Pipeline builds commit-tagged image, pushes to local registry only after tests, target pulls same reference, deploys, health passes (A12) | Registry catalog, digest, pipeline log | T10, T11 |
| T13 | Ansible configuration management | Inventory, vars, templates, idempotent playbook; syntax check, check mode, first real run on isolated target | Playbook, first run log | T11 |
| T14 | Provisioning, reliability, recovery | Clean target provisioned and app deployed; second run `changed=0`; health verified; failed candidate rolled back to previous image (A13–A14) | Two run logs, health and rollback logs | T12, T13 |
| T15 | End-to-end release, docs, viva | Full commit→prod path executed; final docs, troubleshooting, security notes, limitations, future work, demo runbook, viva Q&A; `docs/evidence/INDEX.md` complete (A15) | Final tag, INDEX.md, report outline | T1–T14 |
