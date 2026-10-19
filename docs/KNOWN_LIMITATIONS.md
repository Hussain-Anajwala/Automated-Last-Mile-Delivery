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
| Deployment | Not yet containerised/deployed (Phases 8, 11–14 blocked on Docker — BLOCKERS B2) | Runs locally via `DevApplication` with embedded PostgreSQL. |
| Rollback | Database migrations are forward-only; see Phase 14 notes when written | V1 is the only migration so far. |
