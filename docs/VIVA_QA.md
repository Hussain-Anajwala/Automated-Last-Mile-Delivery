# Viva Preparation — Questions and Answers

Answers describe what this project actually does; evidence paths are in `docs/evidence/INDEX.md`.

## Design

**Why a modular monolith and not microservices?**
The MVP has three tables and one team. A monolith with clear packages (`delivery`, `workflow`,
`dashboard`, `security`, `api`) keeps one transaction for status + history and one deployable unit,
which keeps the DevOps pipeline simple. Splitting would add network failure modes without benefit.

**Where are the business rules?**
In `DeliveryService` and `DeliveryPolicy`. Controllers (web and API) only translate HTTP. The same
policy decides both what the server accepts and which buttons the page shows, so a visible control is
always one the server allows.

**How is the status machine enforced?**
`DeliveryStatus.allowedNext()` defines it; the service checks the transition, role and ownership,
then updates the row and inserts a history row in one `@Transactional` method. A test forces the
history insert to fail and confirms the status change is rolled back. The database also has CHECK
constraints (e.g. FAILED requires a reason, ASSIGNED requires an agent) as a second line.

**Why can't an agent see another agent's delivery even with a crafted URL?**
Ownership is checked in the service after loading the record; a foreign record returns 404 (not 403)
so its existence is not revealed. Search forces the agent filter to the caller's own id, whatever the
request contains.

**Why HTTP Basic for the API?**
It is stateless and simple for scripts. The API chain ignores session cookies, which is what makes
excluding it from CSRF safe — a cross-site request cannot reuse the browser session. The original
design allowed cookies too; that was found unsafe during implementation and changed (D9 revised).

## DevOps

**What triggers Jenkins?**
A multibranch job scans the GitHub repository every 2 minutes and builds branches whose head
changed. A webhook would require exposing Jenkins to the internet, which we avoid.

**What stops a bad commit from being deployed?**
Stage order. Parameter validation, unit/integration tests and the Selenium gate run before images
are built. Any failure skips packaging, publishing and deployment — shown in builds #6 and #9.

**How do you know the deployed container is the commit you tested?**
Images are tagged with the 12-character commit SHA and carry an OCI `revision` label; after deploying,
the pipeline compares the running container's label with the commit and fails otherwise. In the
registry the pushed digest is compared with the registry's `Docker-Content-Digest`.

**Why not `latest`?**
It is mutable: you cannot tell what is running or roll back to a known version. Every release is an
immutable tag kept in the registry.

**How does rollback work and what are its limits?**
Before deploying, the pipeline records the running image references. If the candidate is not healthy
(compose `--wait` or readiness/revision check fails), it redeploys the recorded release and verifies
it, and still marks the build failed. Build #15 demonstrated this with an injected database
misconfiguration. Limits: data is not rolled back; Flyway migrations are forward-only, so a release
with a destructive migration could not be rolled back by image alone — migrations must stay additive.

**Why does the Jenkinsfile verify through the Nginx container instead of curl to localhost:8088?**
The published port is bound to the Windows host's loopback; from inside the Jenkins container
`localhost` is Jenkins itself. Running `wget` inside the Nginx container tests the real path
Nginx → app → database.

**Why is the registry address different for Jenkins?**
Network namespaces: the Docker daemon reaches it as `localhost:5000`, the Jenkins container as
`lastmile-registry:5000`. Identity is therefore proven by digest, not by name.

**Why one Jenkins executor?**
The workstation ran out of Windows commit memory twice; one executor keeps heavy builds sequential.

**What did Selenium catch?**
A renamed form field (`failureReason` instead of `reason`) that no MockMvc test could see, because
those tests post parameters directly. Build #9 failed at the Selenium gate; the fix added a MockMvc
regression test so the faster layer catches it too.

**How do you avoid flaky browser tests?**
`data-testid` locators, explicit waits only, fresh data and browser per test, instant scrolling, and
waiting for a genuinely new document after each click. One real flake was diagnosed and fixed; then
five consecutive runs passed.

## Ansible

**What does the playbook guarantee?**
Packages, UTC, firewall (SSH allowed first), Docker from the official repository, daemon config,
a non-login service account, `/opt/lastmile` with correct permissions, the same compose file as the
pipeline, a 0600 secrets file, the exact image tag pulled and started, readiness and revision checks,
and a current/previous release record.

**How is idempotency designed and proven?**
Every command task declares `changed_when`; templates and copies only change when content differs;
the Docker restart is a handler. On the WSL2 target, run 1 reported `changed=13` and the identical
second run `changed=0`, with the containers not restarted (`04-run2-idempotency.log`).

**What did running against a real host teach you?**
It found a defect that syntax checks and lint did not: `regex_search` returns a string, and
ansible-core 2.21 rejects non-boolean conditionals. The offline test had checked an equivalent rule
rather than the exact expression; it now checks the identical one.

**Is the WSL2 target a real separate node?**
It has its own root filesystem, systemd, SSH server and Docker engine, and cannot see Windows drives,
but it shares the WSL kernel and network stack with Docker Desktop. That is why its firewall is off and
it uses its own port and Docker address ranges. Whether that satisfies the "clean node" requirement is
the teacher's decision, which is still pending.

**Why not run Ansible against Windows or the control node itself?**
The assignment asks for a managed Linux node. The playbook asserts an Ubuntu target, and the
validation playbook only uses `assert`/`template`/`debug` so it changes nothing locally.

## Security and honesty

**How are secrets handled?**
They live in a git-ignored `.env`, are injected as Jenkins credentials (masked in logs), and reach
containers as environment variables at runtime — never in images, build args or Git. A fail-closed
gitleaks gate runs before commits and pushes.

**Was there a security incident?**
Yes, a process one: a scan result was piped into `tail`, so a push proceeded despite "leaks found".
The findings were public plugin versions; nothing secret leaked. The gate now fails closed on findings,
scanner errors or a missing scanner, with a 10-check test.

**What is not finished?**
Teacher confirmation that a WSL2 target is acceptable; an independent second-person PR review if the
teacher requires one; one continuous end-to-end run of the final commit through to the target; any
public hosting (none — local only).
