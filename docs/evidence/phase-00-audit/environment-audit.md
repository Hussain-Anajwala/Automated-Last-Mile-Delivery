# Environment Audit — 2026-10-19

Captured by Claude Code (VS Code extension, Windows host) before any project changes.
Personal account details (emails, tokens) are redacted. Commands were run in Git Bash
(`bash`) or Windows PowerShell 5.1 (`ps`) as labelled.

## Host

| Item | Observed value | Command |
|---|---|---|
| OS | Windows 11 Home, build 26300 | ps `Get-CimInstance Win32_OperatingSystem` |
| CPU | Intel Core i7-9750H, 6 cores / 12 threads | ps `Get-CimInstance Win32_Processor` |
| RAM | 15.8 GB total, **0.6 GB free** at audit time (browsers/VS Code using several GB) | same |
| Disk C: | **9.7 GB free** / 208.6 GB used | ps `Get-PSDrive` |
| Disk D: | 855.5 GB free | same |
| Hypervisor | Present (WSL2 platform active) | ps `Win32_ComputerSystem.HypervisorPresent` → `True` |
| Claude Code location | VS Code extension on Windows (not inside WSL2 Ubuntu) | session environment |

## Repository

| Item | Observed value |
|---|---|
| Path | `C:\Users\husai\Desktop\Devops` |
| Branch | `main`, working tree clean, no untracked files |
| History | 1 commit: `e9715d0 docs: add project planning and execution instructions` |
| Remote | `origin` → `https://github.com/Hussain-Anajwala/Automated-Last-Mile-Delivery.git` |
| Remote state | Repository exists, **public**, **empty** (`git ls-remote origin` returned no refs, exit 0; GitHub API `size=0`) |
| Upstream tracking | `main` has no upstream; the planning commit has not been pushed |
| `core.autocrlf` | `true` (LF must be enforced with `.gitattributes`) |
| Credential helper | Git Credential Manager (`credential.helper manager`) |

## Toolchain

| Tool | Result | Notes |
|---|---|---|
| Java | `java version "22.0.2"` (Oracle, `C:\Program Files\Java\jdk-22`) | **JDK 21 LTS not installed**; `JAVA_HOME` unset |
| Maven | `mvn: command not found` | Expected; Maven Wrapper will be committed |
| Git | 2.49.0.windows.1 | OK |
| Docker CLI | 29.4.1 | OK |
| Docker daemon | `failed to connect to ... dockerDesktopLinuxEngine` | **Docker Desktop not running** (`com.docker.service` Stopped/Manual; `docker-desktop` WSL distro Stopped) |
| Docker Compose | v5.1.3 | Client plugin present |
| Node.js / npm | v22.16.0 / 10.9.2 | Not required by the app |
| GitHub CLI `gh` | `command not found` | **Not installed** |
| Python | 3.13.3 (`python`), 3.14.3 (`python3`) | OK |
| Ansible | `command not found` on Windows | Expected; must run in WSL2 Ubuntu |
| OpenSSH client | OpenSSH_9.9p2 | OK |
| curl | 8.12.1 | OK |
| Chrome | 155.0.8059.81 | Usable for headless Selenium |
| Edge | 155.0.4283.62 | Alternative browser |
| WSL | Default version 2; only distro `docker-desktop` (Stopped) | **No Ubuntu distro** for Ansible control node |
| VM software | VirtualBox / VMware / Multipass not found | Windows 11 **Home** has no Hyper-V Manager |
| Jenkins | Not installed (no service) | Topology chosen in Phase 3 |

## Services and ports

| Port | Owner | Impact |
|---|---|---|
| 5432 | Native Windows service `postgresql-x64-16` (Running, Automatic) | Compose PostgreSQL must not publish host port 5432; user's service will not be stopped |
| 80, 443, 5000, 5001, 8080, 8081, 8443, 9000, 50000, 15432 | Free at audit time | Candidate ports for Nginx, registry, Jenkins |

## Network

| Check | Result |
|---|---|
| `HEAD https://repo.maven.apache.org/maven2/` | 200 |
| `GET https://api.github.com/repos/<owner>/Automated-Last-Mile-Delivery` | 200 (public, empty) |

## Readiness verdict

**READY WITH BLOCKERS.** Phases 1–2 (documentation/planning) have no environment
dependency and can proceed. Phase 3 compatibility gates for JDK 21, Docker, Jenkins,
WSL2 Ansible and an isolated Ubuntu target are blocked until the items in
`docs/BLOCKERS.md` are resolved.
