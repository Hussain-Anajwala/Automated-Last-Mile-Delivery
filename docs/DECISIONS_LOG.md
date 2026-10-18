# Decisions Log

Format: ID — date — context — options — decision — rationale — consequences.

---

## D1 — 2026-10-19 — Do not use the native PostgreSQL service on port 5432

- **Context:** The audit found the user's own Windows service `postgresql-x64-16` listening on 5432.
- **Options:** (a) stop/reuse the user's service; (b) run project PostgreSQL in Docker Compose on a different or unpublished host port.
- **Decision:** (b). The Compose PostgreSQL container will not publish host port 5432; if host access is needed for development it will use `15432`.
- **Rationale:** Avoids altering the user's existing database and keeps the approved Compose topology reproducible.
- **Consequences:** Documented connection strings use the Compose service name inside the network and `localhost:15432` from the host.

## D2 — 2026-10-19 — Install JDK 21 user-locally rather than use JDK 22

- **Context:** Only JDK 22.0.2 (non-LTS, no longer updated) is installed; the approved stack is Java 21 LTS.
- **Options:** (a) build with JDK 22 targeting release 21; (b) install a JDK 21 LTS distribution without admin rights.
- **Decision:** (b), Eclipse Temurin 21 ZIP in a user-owned folder (see `docs/BLOCKERS.md` B1), executed in Phase 3.
- **Rationale:** Matches the approved stack and the JDK used by Docker images and Jenkins, avoiding toolchain drift.
- **Consequences:** Builds set `JAVA_HOME` to the JDK 21 folder; JDK 22 stays installed but unused by the project.

## D3 — 2026-10-19 — Use the existing public GitHub remote

- **Context:** `origin` points to an existing, empty, public repository owned by the user.
- **Decision:** Use it as the project repository (CLAUDE.md: use the configured remote; do not create a replacement). No visibility change.
- **Consequences:** Everything pushed is public, so secret scanning before every push is mandatory.

## D4 — 2026-10-19 — Claude Code runs on Windows, not WSL2

- **Context:** This session runs in the VS Code extension on Windows (Git Bash + PowerShell), while the plan *prefers* Claude Code in WSL2 Ubuntu.
- **Decision:** Continue on Windows for application, Maven and Docker work; run Ansible through `wsl -d <Ubuntu distro> -- ...` once B3 is resolved, labelling those commands as Ubuntu/WSL.
- **Consequences:** Documentation provides both `mvnw.cmd` (PowerShell) and `./mvnw` (Ubuntu/WSL) commands.

## D5 — 2026-10-19 — Provisional defaults for teacher questions

- **Context:** `docs/BLOCKERS.md` B6 lists questions only the teacher can answer.
- **Decision:** Until answered: local deployment URL; seeded accounts plus admin-only read-only user list; Dockerized Spring Boot JAR behind Nginx; Ansible target per B4 option A unless the user/teacher chooses B.
- **Consequences:** Revisit if the teacher answers differently; affected phases will be noted.
