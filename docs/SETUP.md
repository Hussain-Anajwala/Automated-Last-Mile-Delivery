# Local Setup

## Prerequisites

| Tool | Version | Notes |
|---|---|---|
| JDK | Temurin 21 LTS | Project uses `D:\tools\jdk-21` (see BLOCKERS B1). Any JDK 21 works if `JAVA_HOME` points to it. |
| Maven | none | The committed wrapper downloads Maven 3.9.16 and verifies its SHA-256. |
| Chrome or Chromium | current | Only for Selenium tests (`-Pselenium`). |
| Docker Desktop | 29.x with Compose v5 | Only for container/deployment phases. |

No database installation is needed for tests: they start an embedded PostgreSQL 17 (decision D11).

## Build and test

Windows PowerShell (from the repository root):

```powershell
. .\scripts\use-jdk21.ps1        # sets JAVA_HOME for this session only
.\mvnw.cmd -B verify             # compile, unit + integration tests, package
```

Ubuntu / WSL (bash):

```bash
export JAVA_HOME=/path/to/jdk-21
./mvnw -B verify
```

The executable JAR is written to `target/lastmile-delivery.jar`.

## Health endpoints

- `GET /api/v1/health/live` → `200 {"status":"UP"}`
- `GET /api/v1/health/ready` → `200 {"status":"UP"}` when the database answers, otherwise `503 {"status":"DOWN"}`

## Configuration

All runtime settings come from environment variables; see [.env.example](../.env.example).
Copy it to `.env` (git-ignored) for local Compose use. There are no default passwords.
