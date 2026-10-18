# Product Requirements Document (PRD)

**Project:** Automated Last-Mile Delivery Management Platform  
**Document status:** Approved planning baseline; verify against the teacher rubric before implementation  
**Version:** 1.1  
**Product label used in UI mockups:** LastMile Ops (working label; official project title remains unchanged)

## 1. Product summary

The Automated Last-Mile Delivery Management Platform is a web application for recording, assigning, searching and monitoring deliveries during the final stage of fulfilment. It provides role-based operational actions, a constrained delivery-status workflow, an operational summary dashboard and a status audit trail. The project also demonstrates a repeatable DevOps lifecycle from Git commit through build, tests, Docker packaging, deployment, configuration management, health verification and recovery.

This is a semester-scale MVP and a learning/demo system, not a production logistics platform. The operational data will be fictional or seeded for demonstrations.

## 2. Problem statement

Small delivery teams may track deliveries using spreadsheets, chat messages or disconnected manual records. This can lead to inconsistent statuses, duplicate records, difficult record lookup, weak accountability and limited visibility into delivery outcomes. A centralized platform can establish a common record, enforce who may perform each action and present current delivery counts in one place.

A second problem is operational: software that works only on a developer's laptop is difficult to reproduce and release safely. The project addresses this by creating an automated build/test/deploy workflow and demonstrating repeatable configuration and release recovery.

## 3. Goals and measurable objectives

1. Provide create, view, update, list and search operations for delivery records.
2. Authenticate users and enforce three roles: `ADMIN`, `DISPATCHER` and `AGENT`.
3. Enforce valid delivery-state transitions on the server and record each transition.
4. Present dashboard counts derived from persisted delivery data, scoped by role where appropriate.
5. Use Git/GitHub branches, pull requests, reviews, conflict resolution and release tags.
6. Automate Maven build and automated quality checks through Jenkins.
7. Make browser-level Selenium tests a blocking quality gate before deployment.
8. Package the application as a versioned Docker image and publish it to a local registry.
9. Use Ansible to configure a Linux target, demonstrate idempotency, verify health and demonstrate rollback/recovery.
10. Produce auditable technical documentation and demonstration evidence for all 15 teacher tasks.

### Success criteria

| Area | Acceptance target |
|---|---|
| Delivery CRUD | Authorized users can create, view and edit permitted fields; invalid input is rejected clearly. |
| Search | Search by delivery code, recipient name and order/reference value works; status/agent filters work. |
| Access control | Unauthorized page/API requests are rejected server-side; agents see only their own assigned deliveries. |
| Workflow | Only permitted state transitions succeed; each successful transition adds a history entry in the same transaction. |
| Dashboard | Counts match database records for the logged-in role's scope. |
| Build | A clean checkout builds using the committed Maven Wrapper and documented JDK. |
| Automated testing | Unit/integration tests and 3–5 critical Selenium journeys pass in a clean test run. |
| CI/CD | Jenkins runs from a `Jenkinsfile`; a failing mandatory test blocks deployment. |
| Container | A versioned image runs with configuration injected externally; logs and lifecycle are demonstrable. |
| Registry | Image can be pushed to and pulled from the local registry using a documented address reachable by all pipeline participants. |
| Configuration management | Ansible provisions the selected Linux target; a second run reports no unintended changes. |
| Recovery | A prior known-good image/configuration can be restored and passes a health check. |
| Documentation | Setup, architecture, data model, test strategy, deployment, troubleshooting, limitations and future work are documented. |

Targets above are proposed acceptance criteria, not claims of measured performance or availability.

## 4. Stakeholders and users

| Stakeholder / role | Needs |
|---|---|
| Administrator | Operational oversight and user/role administration if included in the approved scope. |
| Dispatcher / operations staff | Create, update, assign, search and monitor deliveries. |
| Delivery agent | View assigned deliveries and submit allowed delivery outcomes. |
| Developer / DevOps engineer | Maintain code, automated checks, packaging and deployments. |
| Subject teacher / evaluator | Inspect features, Git workflow, CI/CD gates, provisioning and evidence. |
| Hypothetical delivery business | More consistent records and a summary of operational outcomes. |

## 5. Personas and primary user stories

- **As a dispatcher**, I can create a delivery and assign it to an active agent so that work is traceable.
- **As a dispatcher**, I can search by delivery code, recipient or reference so that I can locate a record quickly.
- **As a delivery agent**, I can view only deliveries assigned to me and advance them through permitted statuses.
- **As a delivery agent**, I must provide a reason when I mark a delivery failed so that the outcome is actionable.
- **As an administrator**, I can view all operational deliveries and review user roles/accounts according to the final agreed minimal admin scope.
- **As an evaluator**, I can see a Jenkins run fail when a mandatory test fails and succeed after the defect is fixed.
- **As a DevOps engineer**, I can deploy a versioned image and recover a previous stable version without rebuilding an unknown artifact.

## 6. MVP functional scope

### Included

1. Login and logout; seeded demonstration accounts for the three roles.
2. Role-aware dashboard and navigation.
3. Delivery creation with server-generated unique delivery code.
4. Delivery list, detail view, editing of allowed non-system fields and search/filter/pagination.
5. Assignment/reassignment by authorized roles.
6. Delivery status workflow: `CREATED`, `ASSIGNED`, `OUT_FOR_DELIVERY`, `DELIVERED`, `FAILED`.
7. Status history with actor and timestamp; failure reason required for `FAILED`.
8. Dashboard summary cards for total deliveries and relevant status counts.
9. Application health endpoint and consistent input/error handling.
10. Tests and deployment/operations configuration required by the 15-task assignment.

### Excluded from the MVP

- Live GPS tracking, maps and route optimization.
- AI-based assignment or demand forecasting.
- Customer-facing tracking portal or native mobile app.
- SMS, WhatsApp, email or push notifications.
- Payments, cash-on-delivery reconciliation or refunds.
- Real e-commerce/ERP/courier integrations.
- Multi-tenant SaaS functionality, service mesh or microservices.
- Public production hosting as a mandatory dependency.
- Full enterprise user provisioning and password-reset email workflows.

## 7. Delivery workflow rules

Allowed transitions:

- `CREATED` → `ASSIGNED`
- `ASSIGNED` → `OUT_FOR_DELIVERY`
- `OUT_FOR_DELIVERY` → `DELIVERED` or `FAILED`
- `FAILED` → `ASSIGNED` only after an authorized reassignment/retry decision
- `DELIVERED` is terminal in the MVP

The transition is validated in the service layer and saved together with its history entry in one database transaction. Hiding a button in the UI is not sufficient authorization. Administrative exceptions, if enabled, must be explicit and audited.

## 8. Role and permission requirements

| Capability | ADMIN | DISPATCHER | AGENT |
|---|---|---|---|
| View all deliveries | Yes | Yes | No; assigned records only |
| Create delivery | Yes | Yes | No |
| Edit permitted delivery details | Yes | Yes | No |
| Assign/reassign agent | Yes | Yes | No |
| Change status | Explicit audited override only, if allowed | Valid operational transitions | Valid transitions on own assigned records |
| View dashboard | Global | Operational | Own-delivery summary |
| Manage roles/accounts | Minimal seeded-account administration, if approved | No | No |

Exact edge cases must be covered by authorization tests. For scope control, seed the initial demo accounts through a documented development-only mechanism; avoid building a full identity-management product.

## 9. Non-functional requirements

- **Security:** password hashes only; parameterized persistence operations; server-side role checks; no secrets committed; least-privilege deployment.
- **Data integrity:** foreign keys, unique delivery code/email, validation and transactional state/history updates.
- **Maintainability:** controller/service/repository separation, clear naming and focused modules.
- **Usability:** responsive screens, explicit loading/empty/error/success states, accessible labels and keyboard focus.
- **Testability:** deterministic seed data, stable browser selectors and repeatable local test commands.
- **Portability:** build from a fresh Windows checkout; Linux deployment via a container image.
- **Reliability evidence:** health checks, image version traceability, idempotent configuration and documented recovery.
- **Privacy:** use fictional data; avoid logging recipient phone/address details unnecessarily.

## 10. Constraints and assumptions

- Developer workstation: Windows, using PowerShell and Docker Desktop with the WSL2 backend.
- Java 21 LTS is the proposed baseline; exact Spring Boot, Maven plugin and dependency versions must be checked for compatibility and pinned during Phase 3.
- UI uses Spring MVC/Thymeleaf and Bootstrap; visual design is implemented directly in Thymeleaf/Bootstrap; no external design tool is required.
- Docker Compose runs local dependencies and the deployable application stack.
- The registry is local and is not assumed to be reachable from outside the workstation/LAN.
- Ansible operates against a Linux target. Primary target is a dedicated local Ubuntu LTS VM reachable over SSH; an isolated Ubuntu WSL2 target is the fallback if VM constraints make the primary target impractical.
- No external paid cloud service is required for the core demonstration.
- Teacher instructions and lab policies take precedence if they mandate a specific deployment target or tool variant.

## 11. MVP Definition of Done

A feature is done only when its acceptance criteria pass, input and error handling are implemented, authorization is enforced on the server, relevant automated tests pass, documentation is updated, changes are reviewed/merged using Git, and evidence is recorded. A visual mockup alone is not a feature. A test or deployment must not be reported as successful without actual logs/results.

## 12. Risks to product scope

The main scope risk is adding features that do not help satisfy the 15-task rubric. Keep route optimization, GPS, external integrations and sophisticated user lifecycle features out of the MVP. Prioritize the delivery workflow and verifiable DevOps evidence over ornamental UI features.
