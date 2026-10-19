# Security Notes and Threat Model

Scope: a local semester MVP with fictional data. These notes describe what is enforced, how it is
verified, and what is knowingly out of scope.

## Assets and trust boundaries

| Asset | Where | Protection |
|---|---|---|
| Delivery records (names, phone, address — fictional) | PostgreSQL volume `lastmile_pgdata` | DB on an internal Docker network, no published port; app-level authorization |
| User credentials | `users.password_hash` | Delegating encoder (BCrypt); never logged; generic login error |
| Deployment secrets (DB password, demo password) | `ci/jenkins/.env` (git-ignored) → Jenkins credentials → container env | Never in Git, images, build args or console logs (checked) |
| Jenkins admin | `ci/jenkins/.env` | Jenkins bound to 127.0.0.1; anonymous access denied (403) |
| Images | Local registry `127.0.0.1:5000` | Loopback only; no auth/TLS, so never exposed |

## Threats and controls

| Threat | Control | Verified by |
|---|---|---|
| Agent reads/changes another agent's delivery | Scoped queries; ownership check in the service; 404 instead of 403 to avoid disclosure | `WebAuthorizationTest`, `DeliveryApiTest`, Selenium J3 |
| Hidden-button bypass (crafted URL/POST) | Service re-checks role and ownership on every operation; URL rules as a first layer | crafted-request tests |
| Mass assignment of system fields | Form/DTO without system fields + `@InitBinder` allow-list | `systemManagedFieldsSuppliedByTheClientAreIgnored`, API test |
| Stale sessions after deactivation/role change | Actor re-read from DB on every operation | `aSessionWhoseAccountWasDeactivatedOrChangedRoleIsRefused` |
| CSRF | Enabled on all browser routes; API is stateless Basic only, so cookies cannot ride on it | `submissionWithoutCsrfTokenIsRejected`, `browserSessionCookiesAreNotAcceptedByTheApi` |
| SQL injection / LIKE abuse | JPA parameter binding; LIKE wildcards escaped; search text ≤ 100 chars server-side | `likeWildcardsInTheQueryAreTreatedLiterally`, `overlongSearchTextIsRejected` |
| Double submit / lost update | Optional `expectedVersion` token + `@Version` | `staleVersionIsRejectedSoADoubleSubmitCannotApplyTwice` |
| Information leakage | No stack traces/messages in error pages; minimal health payload; phone/address not logged | config + error templates |
| XSS / clickjacking | Thymeleaf escaping; CSP `default-src 'self'`, `frame-ancestors 'none'` | security headers config |
| Container escape impact | Non-root user 10001, read-only rootfs, all capabilities dropped, no-new-privileges, memory limit | `docker inspect` in `phase-11-docker` |
| Secrets committed to the public repo | Fail-closed gitleaks gate before commit and push | `scripts/test-secret-gate.sh` (10 checks) |
| Unhealthy release stays live | Readiness includes DB; pipeline verifies and rolls back automatically | Jenkins #15 drill |

## Accepted risks (documented, local-only scope)

- Jenkins has the Docker socket (root-equivalent on the Docker VM): acceptable only because it is local and single-user.
- Registry without authentication/TLS: loopback only; a remote target would use a host-only network.
- No login rate limiting or account lockout.
- HTTP only (no TLS) on 127.0.0.1; session cookie therefore not marked `Secure`.
- Chromium runs with `--no-sandbox` inside the Jenkins container (it only visits the local test app).

## Incident record

2026-10-19 03:32 IST — a push proceeded despite a "leaks found" result because the scan was piped
into `tail`. All 23 findings were public plugin version strings; nothing secret was published. The
gate now fails closed (including on scanner errors) and is tested. Details: `docs/IMPLEMENTATION_LOG.md`.
