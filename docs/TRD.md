# Technical Requirements Document (TRD)

**Project:** Automated Last-Mile Delivery Management Platform  
**Status:** Approved planning baseline; validate environment and pin versions in Phase 3  
**Version:** 1.1

## 1. Architecture decision summary

Use a modular monolith. Keep the application simple enough for a semester project, with explicit module boundaries and a shared service layer. Do not split the MVP into microservices.

### Approved stack

| Layer / concern | Decision |
|---|---|
| Language/runtime | Java 21 LTS, with exact JDK distribution documented for developers and Jenkins |
| Framework | Spring Boot release compatible with chosen JDK |
| Web | Spring MVC, Thymeleaf templates, HTML/CSS, Bootstrap and small amounts of JavaScript |
| Authentication | Spring Security form login/session, BCrypt or a current Spring Security recommended password encoder |
| Persistence | Spring Data JPA / Hibernate |
| Database | PostgreSQL |
| Schema migrations | Flyway; avoid reliance on Hibernate auto-create in deployment |
| Build | Maven Wrapper committed to repository |
| Unit/integration testing | JUnit 5, Spring Boot Test; Testcontainers only if justified and workable on the target workstation |
| Browser testing | Selenium WebDriver with headless browser for repeatability; Maven Surefire/Failsafe as appropriate |
| CI/CD | Jenkins Pipeline-as-Code (`Jenkinsfile`) |
| Packaging/runtime | Docker image; Docker Compose for the local environment |
| Registry | Local Docker Registry container with documented port/name and reachability from Jenkins, deployment target and host |
| Web entry point | Nginx reverse proxy to Spring Boot container |
| Provisioning | Ansible inventory and YAML playbook, controlled from Ubuntu under WSL2 |
| Source control | Git, GitHub, pull requests, issue templates, tags and release branch policy |
| UI/UX | Code-first design using Thymeleaf, Bootstrap and shared CSS tokens; running application is the source of truth |
| AI-assisted workflow | Claude Code using repository `CLAUDE.md`, this planning pack and the master execution prompt |

Versions are not all frozen in this planning document. In Phase 3, check current official compatibility for Java/Spring Boot, Maven plugins, Selenium/browser, Jenkins Java requirements, Docker Compose and Ansible, then commit exact versions and reproducible setup instructions. Do not upgrade dependencies opportunistically in later phases.

## 2. Logical application architecture

```text
Browser
  └── Nginx (HTTP entry point / reverse proxy)
        └── Spring Boot application
              ├── Web controllers (Thymeleaf views)
              ├── REST controllers (health and documented JSON contracts)
              ├── Application services (business rules and transitions)
              ├── Spring Security (authentication and authorization)
              ├── Repositories (Spring Data JPA)
              └── Flyway migrations
                    └── PostgreSQL
```

The business rules must live in application services, not in templates or duplicated controller code. Web controllers and REST controllers may share the same service layer. Health checks should distinguish process liveness from readiness/dependency health; a database outage must not produce a misleading "healthy" response.

## 3. Local and deployment topology

### Developer workstation

- Windows 10/11 (exact edition and available virtualization capability to be recorded), PowerShell, Git, supported JDK 21 and Docker Desktop using WSL2 backend.
- Keep source code in a stable path; if building inside WSL2, prefer the Linux filesystem for better file I/O.
- Standardize LF endings for `Jenkinsfile`, Dockerfiles, shell scripts and Ansible files via `.gitattributes`; Windows-specific scripts may use PowerShell conventions.
- `mvnw.cmd` is the standard local build entry point; do not require a globally installed Maven version.

### Local application stack

Docker Compose manages PostgreSQL, the application container, Nginx and the local registry. Jenkins may run as a local Jenkins service or a controlled container depending on what the Phase 3 compatibility spike proves. Select one topology, document it, and do not maintain multiple competing setups. If Jenkins runs in a container and needs Docker daemon access, treat access to the Docker socket/daemon as high privilege and keep that instance local and trusted.

### Ansible target

Windows is not the target for apt/systemd-style provisioning. The primary target is a dedicated local Ubuntu LTS VM reachable by SSH. If virtualization is unavailable, use a deliberately isolated Ubuntu WSL2 target distribution and document how it is separated from the developer environment. The control node is Ubuntu under WSL2 or a documented Ansible runner. Do not point provisioning tasks at an everyday workstation without review.

### Registry naming/reachability

The local registry hostname is not assumed to be identical inside every network namespace. The Phase 3/12 setup must prove that Jenkins can push and the target can pull the same image reference. Document host ports, DNS/host aliases and any Docker Desktop settings. Never put a private registry password into source control. The registry must not be exposed to public networks.

## 4. Application modules

1. `auth`: login, logout, current-user context and access-denied handling.
2. `users`: seeded demo accounts, active state and roles; only minimal admin management if confirmed in scope.
3. `deliveries`: create/list/detail/edit/search/filter and assignment.
4. `workflow`: transition policy, reason validation, event history and timestamps.
5. `dashboard`: status aggregations scoped by role.
6. `health`: liveness and readiness endpoints suitable for Docker health checks.

Suggested Java packages: `config`, `security`, `user`, `delivery`, `workflow`, `dashboard`, `health`, with DTOs/forms separated from JPA entities. Keep entities out of public JSON responses.

## 5. Routes and API contracts

The UI is primarily server-rendered. Web page routes may include:

- `GET/POST /login`; `POST /logout`
- `GET /dashboard`
- `GET /deliveries`; `GET/POST /deliveries/new`
- `GET /deliveries/{id}`; `GET/POST /deliveries/{id}/edit`
- `POST /deliveries/{id}/assignment` (authorized assignment/reassignment)
- `POST /deliveries/{id}/status` (server validates transition)
- `GET /admin/users` if minimal user administration remains in scope

Documented JSON API:

| Method | Endpoint | Purpose | Access |
|---|---|---|---|
| GET | `/api/v1/health/live` | Process liveness | Health check |
| GET | `/api/v1/health/ready` | Readiness including required dependencies | Health check / monitoring |
| GET | `/api/v1/deliveries` | List/search/filter deliveries with pagination | Authenticated and role-scoped |
| POST | `/api/v1/deliveries` | Create delivery | ADMIN, DISPATCHER |
| GET | `/api/v1/deliveries/{id}` | Read a delivery | Authenticated and role-scoped |
| PUT | `/api/v1/deliveries/{id}` | Update editable non-system fields | ADMIN, DISPATCHER |
| PATCH | `/api/v1/deliveries/{id}/assignment` | Assign/reassign an agent | ADMIN, DISPATCHER |
| PATCH | `/api/v1/deliveries/{id}/status` | Request a state transition | Role, ownership and transition checks |
| GET | `/api/v1/dashboard/summary` | Role-scoped summary counts | Authenticated |

The UI and REST API must share application services and validation policies. Use DTOs, explicit error responses and server-generated fields. The endpoint catalogue can be refined during Phase 3, but any changed contract must be reflected in the schema and test documents.

## 6. Security requirements

- Hash passwords; do not store or log plaintext passwords.
- Seed demo credentials only for local/test use, document them as non-production, and avoid hard-coded production secrets.
- Enforce role/ownership restrictions at service/controller security boundaries; do not trust a role or actor ID submitted by the client.
- Agents may only view and update eligible deliveries assigned to their account.
- Validate CSRF protection for browser forms; do not disable it globally to make tests easier.
- Use environment variables or a local ignored configuration file for secrets; provide a safe `.env.example` containing placeholders only.
- Do not commit database dumps with real personal information, registry passwords, SSH private keys, or credentials.
- Limit container capabilities and exposed ports; PostgreSQL and registry should not be publicly exposed.
- Logs must avoid unnecessary recipient phone/address details.

## 7. Persistence requirements

Use PostgreSQL and Flyway migration scripts in source control. Enforce unique normalized user email and delivery code; use foreign keys for assignments/history. Keep status update and status-history insert in one transaction. Store timestamps consistently (prefer UTC instants/offset-aware types in Java; display local time clearly). Use explicit indexes for expected searches; optimize only based on measured need.

Core tables: `users`, `deliveries`, `delivery_status_history`. Full field-level rules are in `Backend_Schema.md`.

## 8. Testing strategy

- Unit tests: transition policy, service validation, role/ownership rules and dashboard calculation.
- Integration tests: repositories, migrations, service transactions, authentication/authorization and REST responses.
- Browser tests: 3–5 critical journeys using Selenium and deterministic test data.
- Browser execution should be headless in CI. Pin or consistently manage the browser/driver compatibility. Prefer Selenium Manager only after confirming the runner can download required drivers; otherwise use an explicitly managed browser image/driver.
- Failure screenshots and page-source/log evidence must be captured on Selenium failures, with artifacts published even when the test stage fails.
- Test data must be reset or uniquely generated so reruns remain deterministic.

## 9. CI/CD requirements

Jenkinsfile stages, at minimum:

1. Validate source/checkout and record commit SHA.
2. Build and unit/integration test with Maven Wrapper.
3. Package the executable application artifact.
4. Build or start an isolated test deployment and run Selenium.
5. Publish test reports and archive relevant artifacts on success and failure.
6. Build a Docker image tagged with a traceable commit/build identifier and a human-readable release version when approved.
7. Push to the local registry only after required tests pass.
8. Invoke the approved deployment method and pass environment configuration without committing secrets.
9. Run a readiness/health check.
10. Preserve the last known-good deployment reference; recover on deployment failure or report a controlled rollback step if full automatic rollback cannot safely be completed.

At least one environment setting must be parameterized, such as application port, deployment profile or image tag. Deployment must fail closed: a failed mandatory test cannot publish/activate a release.

## 10. Docker/Nginx requirements

- Multi-stage Dockerfile is preferred if it reduces final image size/attack surface without making the build obscure.
- Run as a non-root application user where practical.
- Configure timezone, database URL, database credentials and active profile externally.
- Provide a health check appropriate to the selected image/runtime.
- Nginx forwards to the app service by Compose/network service name; expose only necessary ports.
- Use named volumes for local database persistence where needed; do not bake persistent records into images.
- Record build args versus runtime environment variables accurately; secrets should be runtime-injected, not Docker build args.
- Demonstrate build, tag, run, inspect, logs, stop, restart and remove commands in the operations guide.

## 11. Ansible requirements

Ansible should manage documented prerequisites and configuration on the Linux target: required packages, deployment user/group, directories and ownership, configuration files, open/service ports as appropriate, Docker availability or dependencies, service state, health verification and release selection. Use variables and templates rather than duplicating literal values. Secrets should be supplied securely and omitted from logs where possible.

Idempotency proof must use a second playbook run with `changed=0` for tasks expected to be unchanged (or explain any inherently changing task). Recovery should identify the previous known-good image/configuration and verify health after restoration.

## 12. Operational documentation and observability

Include README setup instructions, `.env.example`, architecture diagram, API catalogue, role matrix, database/seed-data explanation, local run/test commands, Jenkins setup, registry configuration, container lifecycle commands, Ansible inventory/playbook usage, health/rollback procedure and troubleshooting guide. Store no real secrets in evidence screenshots.

## 13. Compatibility gate before coding

Phase 3 must verify, on the actual Windows machine:

- JDK 21 and Maven Wrapper build.
- Docker Desktop/WSL2 and Compose functioning.
- Jenkins execution environment and Docker CLI/daemon access.
- Headless Selenium browser availability.
- A local registry push/pull using the same image reference from all relevant nodes.
- Ansible connectivity to the isolated Ubuntu target over SSH.
- Port allocation and available memory/disk.

If a selected tool combination fails, document the failure and choose the smallest compatible adjustment before implementing application features. Do not silently swap Java, Docker, the local registry or Ansible for different technologies.
