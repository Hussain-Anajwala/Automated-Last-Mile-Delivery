# Presentation Outline (about 12 slides)

1. **Title** — Automated Last-Mile Delivery Management Platform (LastMile Ops); student name; course.
2. **Problem** — inconsistent spreadsheet/chat tracking; no accountability; releases that only work on one laptop.
3. **Scope** — three roles, five statuses, CRUD/search/assignment/history/dashboard; explicit out-of-scope list.
4. **Architecture** — modular monolith diagram (`docs/ARCHITECTURE.md`); Nginx → Spring Boot → PostgreSQL.
5. **Workflow rules** — state machine diagram; atomic status + history; ownership checks (404 for foreign records).
6. **Live demo** — `docs/DEMO_RUNBOOK.md` part 1.
7. **Git workflow** — branches, PRs #8–#14 with review findings, conflict demo, tag.
8. **CI/CD pipeline** — stage view; quality gates; failure skips deployment (builds #6, #9).
9. **Containers and registry** — non-root layered image; commit-SHA tags; digest verification.
10. **Recovery** — fault injection → automatic rollback (build #15); limits (data, forward-only migrations).
11. **Ansible** — configuration specification; idempotency design; status of target execution.
12. **Lessons, limitations, future work** — below.

## Lessons learned (real events)

- A secret-scan step that does not check exit status is not a gate (incident and fix).
- Browser tests found a defect no unit test could see; flakes had concrete, fixable causes.
- Network namespaces change "localhost": registry names differ, so verify by digest.
- Resource limits are real constraints: one executor, sequential heavy jobs.

## Future enhancements

| Area | Enhancement |
|---|---|
| Product | Edit-history audit, user management UI, login rate limiting/lockout, local-time display |
| Security | TLS (reverse proxy certificate), token-based API auth, registry auth, rootless Docker for CI |
| Pipeline | Separate Jenkins agent, dependency/security scanning (e.g. OWASP Dependency-Check), image signing |
| Operations | Metrics and dashboards (Micrometer/Prometheus), log aggregation, database backups and restore drills |
| Deployment | Real staging vs. production environments; blue-green deploys; expand-contract database migrations |
| Out of MVP scope (PRD) | Maps/GPS, route optimisation, notifications, customer tracking portal, mobile app |
