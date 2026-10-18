# Phase 3 Compatibility Gate — 2026-10-19

Spike project: throwaway Spring Boot 4.1.1 app in the session scratchpad (not committed), one
Flyway migration, one Thymeleaf page, `@SpringBootTest(RANDOM_PORT)` with two tests.

| # | Gate | Result | Evidence |
|---|---|---|---|
| G1 | JDK 21 available | **PASS** | `D:\tools\jdk-21\bin\java -version` → `openjdk version "21.0.12.1" 2026-08-18 LTS` (Temurin); SHA-256 `f9d6e191…8b4e` matched published checksum |
| G2 | Maven Wrapper build with JDK 21 | **PASS** | `mvnw.cmd -v` → `Apache Maven 3.9.16 … Java version: 21.0.12.1, vendor: Eclipse Adoptium`; `mvnw.cmd -B verify` exit **0**, `BUILD SUCCESS` |
| G3 | Spring Boot 4.1.1 + Flyway + PostgreSQL | **PASS** | Log: `Spring Boot :: (v4.1.1)`; `Successfully applied 1 migration … now at version v1`; `select version()` → `PostgreSQL 17.11 on x86_64-windows` (embedded) |
| G4 | Headless Selenium | **PASS** | Selenium 4.43.0, Chrome `155.0.8059.81` headless (`--headless=new`), driver resolved by Selenium Manager; assertion on DB-backed page text passed. `Tests run: 2, Failures: 0, Errors: 0, Skipped: 0` |
| G5 | Docker Desktop / Compose functioning | **PASS (re-checked later on 2026-10-19)** | Initially blocked (B2). After the user started Docker Desktop: `docker run --rm hello-world` → `Hello from Docker!` (exit 0); server 29.4.1, later auto-updated to 29.8.2 |
| G6 | Jenkins with Docker CLI/daemon access | **PASS** | `docker exec lastmile-jenkins docker version` → `docker cli 29.9.1 -> server 29.8.2`; successful Jenkins builds (phase-07 evidence) |
| G7 | Local registry push/pull same reference | **PENDING (Phase 12) / BLOCKED for the target (B4)** | Docker now available; target node not yet chosen |
| G8 | WSL2 Ubuntu Ansible control node | **PASS** | User installed `Ubuntu-24.04`; `~/.venvs/ansible/bin/ansible --version` → `ansible [core 2.21.5]` (no sudo needed) |
| G9 | SSH to isolated Ubuntu target | **BLOCKED (B4)** | No target exists |
| G10 | Ports, memory, disk | **PASS (with risk)** | 5000/8088/8090/15432 free; 5432 used by user's PostgreSQL 16 (avoided, D1); free RAM 1.9–2.9 GB of 15.8 GB; C: 9.8 GB free, D: 855 GB free |
| G11 | GitHub CLI | **PARTIAL (B5)** | `gh version 2.102.0`; `gh auth status` → not logged in (exit 1) |

Commands were run from Git Bash with `JAVA_HOME=D:/tools/jdk-21`. Maven local repository is the
default `~/.m2` on C: (download size observed acceptable; revisit if C: drops below 5 GB).
