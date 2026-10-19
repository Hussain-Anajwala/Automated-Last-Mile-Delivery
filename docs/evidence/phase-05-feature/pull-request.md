# Pull request (prepared — not yet opened)

**Status:** BLOCKED on `docs/BLOCKERS.md` B5 (`gh auth status` → not logged in, 2026-10-19).
No PR number or URL exists yet. Once authenticated, open it with:

```powershell
gh pr create --base develop --head feature/delivery-create `
  --title "feat: delivery creation with validation, generated codes and audit history" `
  --body-file docs/evidence/phase-05-feature/pull-request.md
```

---

## Change

First vertical slice (teacher task 5 / backlog T5): Flyway V1 schema for the three core tables,
form login against `users`, and delivery creation with server-side validation, server-generated
`LMD-<UTC date>-<sequence>` codes, an initial `CREATED` history row and optional assignment to an
active agent (`CREATED → ASSIGNED`, audited). Includes a detail page with status history and a
Docker-free local launcher (`DevApplication`).

## Tests run

```
mvnw.cmd -B verify  ->  Tests run: 36, Failures: 0, Errors: 0, Skipped: 0 — BUILD SUCCESS (exit 0)
```

Live run evidence: `docs/evidence/phase-05-feature/feature-evidence.md`.

## Self-review checklist (author: Claude Code; not an independent review)

- [x] Scope matches T5; no list/search/edit/status endpoints yet (Phase 6)
- [x] Authorization enforced server-side: URL rule + `DeliveryService.requireStaff` re-reads the actor; agents get 403, foreign deliveries 404
- [x] Client cannot set system fields: form has no such properties + `@InitBinder` allow-list; test posts `status`, `deliveryCode`, `createdBy`, `id` and asserts they are ignored
- [x] Validation and error states: field errors, summary, preserved input, CSRF enforced
- [x] Tests added (unit, service, MockMvc, schema constraints, login)
- [x] No secrets: gitleaks staged scan clean; demo password only via env var, verified absent from staged files
- [x] Docs updated (`docs/SETUP.md`, evidence)

### Finding raised and fixed during self-review

`defaultSuccessUrl("/", true)` discarded the saved request after login. Fixed in `aa9b1ab`
with regression test `LoginTest.afterLoginTheUserReturnsToThePageTheyOriginallyRequested`
(failed without the fix, passed with it).

## Risks / notes

- No login rate limiting (MVP limitation, documented).
- Independent review: if the teacher requires a second person, this PR must wait for that
  person's real review. Claude has not reviewed on anyone's behalf.
