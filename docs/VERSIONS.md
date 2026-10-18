# Pinned Versions and Sources (Teacher Task 3)

Verified 2026-10-19 against official channels. Versions are frozen for the semester; upgrades only
through a reviewed issue with regression tests (Implementation_Plan risk R12).

| Component | Version | Source checked | Why this version |
|---|---|---|---|
| JDK | Eclipse Temurin **21.0.12.1+1** LTS | `api.adoptium.net/v3/assets/latest/21/hotspot`; SHA-256 checked against release `.sha256.txt` | Approved Java 21 LTS; latest 21 patch |
| Maven | **3.9.16** (via wrapper) | Maven Central `apache-maven` metadata; SHA-512 verified; SHA-256 pinned in `maven-wrapper.properties` | Latest stable 3.9.x (4.0 is still RC) |
| Maven Wrapper | **3.3.4**, `only-script` | Maven Central `maven-wrapper-distribution`; SHA-1 verified | Latest; no wrapper JAR committed |
| Spring Boot | **4.1.1** | `api.spring.io/projects/spring-boot/releases` (current GA); generations API: OSS support to 2027-07-31 | Current GA; 3.5.x OSS support ended 2026-06-30, 4.0.x ends 2026-12-31 |
| Spring Framework / Security | 7.0.9 / 7.1.1 | Spring Boot 4.1.1 BOM | Managed by Boot |
| Hibernate ORM | 7.4.5.Final | Boot BOM | Managed |
| Flyway | 12.4.0 (+ `flyway-database-postgresql`) | Boot BOM | Managed |
| PostgreSQL JDBC | 42.7.13 | Boot BOM | Managed |
| Thymeleaf / extras-springsecurity6 | 3.1.5.RELEASE / 3.1.5.RELEASE | Boot BOM | Managed by Boot 4.1.1, so supported with Security 7 |
| JUnit Jupiter | 6.0.3 | Boot BOM | Managed |
| Selenium Java | **4.43.0** | Boot BOM (latest upstream 4.51.0, released 2026-10-09) | Boot-managed version proven against Chrome 155 in the spike; avoids overriding the BOM |
| Bootstrap (WebJar) | 5.3.8 | Maven Central `org.webjars:bootstrap` | Served locally, no CDN at runtime |
| Embedded PostgreSQL (tests) | `io.zonky.test:embedded-postgres` 2.2.2 + binaries BOM **17.11.0** | Maven Central | Real PostgreSQL for integration/Selenium tests without Docker; same major as runtime |
| PostgreSQL (runtime) | `postgres:17.11-alpine3.24` | Docker Hub official image tags | Same major/minor as test binaries |
| Runtime base image | `eclipse-temurin:21.0.12.1_1-jre-alpine-3.24` (build: `…-jdk-alpine-3.24`) | Docker Hub official image tags | Matches JDK patch |
| Nginx | `nginx:1.30.6-alpine` | Docker Hub official image | Stable (even-numbered) branch |
| Docker Registry | `registry:3.1.2` | Docker Hub official image | Latest distribution v3 |
| Jenkins | `jenkins/jenkins:2.580.1-lts-jdk21` | `updates.jenkins.io/stable` → `dynamic-stable-2.580.1`; Docker Hub tags | Current LTS on JDK 21 |
| Ansible | `ansible-core` 2.21.5 (Python ≥ 3.12) | PyPI | Installed in WSL Ubuntu 24.04 (Python 3.12) via pipx |
| Docker CLI / Compose (host) | 29.4.1 / v5.1.3 | Observed (`docker version`, `docker compose version`) | Existing Docker Desktop |
| Browser (local) | Chrome 155.0.8059.81 | Observed | Driver resolved by Selenium Manager |
| GitHub CLI | 2.102.0 | winget (hash verified) | Installed at user scope |

## Spring Boot 4 notes that affect the code

- Starters are modular: `spring-boot-starter-webmvc`, `spring-boot-starter-flyway`, and test
  starters such as `spring-boot-starter-webmvc-test` / `spring-boot-starter-security-test`.
- Jackson 3 (`tools.jackson`) is the default JSON library.
