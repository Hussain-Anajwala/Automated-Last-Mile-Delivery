# Known Limitations (MVP)

| Area | Limitation | Why / mitigation |
|---|---|---|
| Accounts | No self-registration, password reset or account editing in the UI; admin list is read-only | Out of MVP scope (PRD §6). Accounts are seeded (demo profile) or provisioned directly in the database. |
| Login | No rate limiting or lockout after failed logins | Local/demo deployment only; noted for future work. |
| API auth | HTTP Basic; credentials sent with each request | Only on localhost/trusted network; a token-based scheme is future work. |
| Audit | Edits of business fields (name, phone, address) are not written to history; only status and assignment changes are | Approved three-table model (D7); an edit-audit table needs scope approval. |
| Search | Case-insensitive `LIKE` without trigram indexes | Adequate for MVP data volumes; no performance target is claimed. |
| Time | All timestamps shown in UTC; delivery codes use the UTC date | Avoids server-time-zone ambiguity; local display is future work. |
| Concurrency | `expectedVersion` token is optional for API clients | Web forms always send it; API clients that omit it rely on the `@Version` column (409 on concurrent commit). |
| Demo data | Seeded deliveries use fictional names and a placeholder phone number | Fictional data policy. |
| Deployment | Local only: Jenkins deploys `main` to Docker Desktop at `http://127.0.0.1:8088`; no public URL (teacher question Q1). The isolated Ansible target is not provisioned yet (B4). | Single environment; `develop` builds and publishes but does not deploy. |
| Rollback | Restores the previous image and configuration, not database contents; Flyway migrations are forward-only | Keep migrations additive (expand/contract) so an older image still works with a newer schema. V1 is the only migration so far. |
| Reviews | PRs #8–#15 carry author self-reviews only | Independent review pending teacher answer (B6 Q2). |
| Resources | Workstation near its memory limit; one Jenkins executor | BLOCKERS B7 (measured). |
