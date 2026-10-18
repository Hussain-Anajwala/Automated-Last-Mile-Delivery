# Automated Last-Mile Delivery Management Platform — Claude Code Planning Pack

This is the approved planning baseline for a semester DevOps project. The pack contains six planning documents, persistent repository instructions for Claude Code, a high-autonomy execution prompt, and a checklist of actions that require the developer's account, machine privileges, or decisions.

**No application code or infrastructure has been implemented by preparing this pack.** Implementation begins only when the developer places these files in the intended project repository and launches Claude Code with `MASTER_PROMPT_CLAUDE_CODE.md`.

## Contents

1. `docs/PRD.md` — problem statement, stakeholders, MVP scope, roles and acceptance criteria.
2. `docs/TRD.md` — application architecture, approved stack, APIs, security, deployment topology and compatibility gates.
3. `docs/UI_UX_Design.md` — code-first visual system, screens, interaction and accessibility requirements. No Stitch dependency.
4. `docs/App_Flow.md` — user journeys, delivery status machine, errors and commit-to-release workflow.
5. `docs/Backend_Schema.md` — entities, relationships, role permissions, validations and API contracts.
6. `docs/Implementation_Plan.md` — 15 teacher-aligned phases, exit gates, evidence plan, risks and reporting templates.
7. `CLAUDE.md` — concise persistent repository rules loaded by Claude Code.
8. `MASTER_PROMPT_CLAUDE_CODE.md` — complete high-autonomy instruction to inspect, plan, implement, test, document and progress through the 15 phases.
9. `USER_SETUP_CHECKLIST.md` — prerequisites, authentication and tasks that may require direct user action.

## Approved decisions

- **Application:** Java 21 LTS, Spring Boot, Spring MVC/Thymeleaf, Bootstrap, Spring Security, Spring Data JPA, PostgreSQL and Flyway.
- **Build/test:** Maven Wrapper, JUnit 5 and Selenium WebDriver.
- **CI/CD:** Jenkins Pipeline-as-Code (`Jenkinsfile`).
- **Runtime:** Docker/Compose, a local Docker Registry and Nginx.
- **Configuration management:** Ansible controlled from Ubuntu under WSL2 and applied to an isolated Ubuntu Linux target.
- **Developer workstation:** Windows 10/11 with WSL2/Ubuntu recommended for Claude Code and Ansible, Docker Desktop using WSL2 integration, and Git.
- **UI/UX:** Code-first implementation and iteration in Thymeleaf/Bootstrap. Google Stitch is not part of the plan.
- **Autonomy:** Claude Code should automatically continue through passing phase gates, pausing only for meaningful blockers, missing access/credentials, destructive or externally consequential actions requiring confirmation, or decisions not supported by the approved requirements.

## Key environment note

Windows is the host workstation, not the target for Linux-specific Ansible tasks. Use an isolated Ubuntu LTS VM reachable over SSH as the preferred provisioning target; an appropriately isolated WSL2 target is a fallback only if it satisfies the teacher's requirements. The local image registry must stay off untrusted networks. Phase 3 must prove that Jenkins can push and the target can pull the exact same image reference.

## Recommended start

1. Complete `USER_SETUP_CHECKLIST.md` and install/authorize Claude Code.
2. Put this pack in the new or intended project repository. Do not overwrite an existing project without checking its current Git status.
3. Launch Claude Code from the repository root.
4. Paste the contents of `MASTER_PROMPT_CLAUDE_CODE.md` as the first task.
5. Let Claude inspect the environment, build a setup/blocker report, and continue with all work that does not depend on unavailable credentials or machine prerequisites.

The prompt favors high autonomy but does not disable security prompts, fabricate evidence, expose secrets, or perform destructive Git operations.
