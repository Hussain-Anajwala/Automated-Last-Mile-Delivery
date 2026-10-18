# Blockers and Required User Actions

Each entry lists the missing prerequisite, why Claude Code cannot complete it, exact steps,
the success signal, and what work continues meanwhile. Status values: `OPEN`, `RESOLVED`.

Source audit: `docs/evidence/phase-00-audit/environment-audit.md` (2026-10-19).

---

## B1 — JDK 21 LTS not installed — RESOLVED 2026-10-19 (automatable)

- **Missing:** Only Oracle JDK 22.0.2 is installed. The approved stack is Java 21 LTS; JDK 22 is a non-LTS release that no longer receives updates.
- **Who acts:** Claude Code, unless the user objects. No administrator rights are needed.
- **Plan:** In Phase 3, download the official Eclipse Temurin 21 LTS Windows x64 ZIP into a user-owned folder (proposed `D:\tools\jdk-21`), verify its checksum, and point `JAVA_HOME` at it for project builds only. The existing JDK 22 is left untouched.
- **Success signal:** `& "D:\tools\jdk-21\bin\java.exe" -version` reports `21.0.x`, and `mvnw.cmd -v` reports Java 21.
- **Resolution:** Temurin `jdk-21.0.12.1+1` ZIP downloaded from the Adoptium GitHub release; SHA-256 `f9d6e191ab098c0d416e7d588a24420a8621cd2f4720dab2459b8b7b2d2d8b4e` matched both the published `.sha256.txt` and the Adoptium API. Extracted to `D:\tools\jdk-21`; `java -version` → `openjdk version "21.0.12.1" 2026-08-18 LTS`. Global `JAVA_HOME`/PATH not modified.

## B2 — Docker Desktop not running; limited RAM and C: disk — OPEN (user)

- **Missing:** Docker daemon is unreachable (`dockerDesktopLinuxEngine` pipe absent). Free RAM was 0.6 GB of 15.8 GB; C: has 9.7 GB free. Docker images, Jenkins, PostgreSQL and a browser will not fit on C: comfortably.
- **Why user:** Starting Docker Desktop, accepting its terms and changing its storage settings are GUI actions on the user's machine.
- **Steps (Windows):**
  1. Close unneeded browser windows before working sessions (several Brave/Chrome processes were using ~3 GB).
  2. Start **Docker Desktop** from the Start menu and wait until it shows *Engine running*.
  3. Recommended: *Settings → Resources → Advanced → Disk image location* → choose a folder on `D:` (for example `D:\DockerDesktop`), then *Apply & restart*.
  4. Recommended: *Settings → Resources* → limit memory to about 6–8 GB if the option is shown (otherwise create `%UserProfile%\.wslconfig` with `[wsl2]` / `memory=8GB` and run `wsl --shutdown`).
- **Success signal (PowerShell):** `docker version` shows a Server section and `docker run --rm hello-world` prints `Hello from Docker!`.
- **Continues meanwhile:** Phases 1–2; Maven/JUnit work once B1 is resolved.

## B3 — No Ubuntu WSL2 distribution (Ansible control node) — OPEN (user)

- **Missing:** Only the `docker-desktop` WSL distro exists. Ansible must run from Ubuntu under WSL2.
- **Why user:** Installing a distro creates a Linux user account and password interactively.
- **Steps (PowerShell):**
  1. `wsl --install -d Ubuntu-24.04` (optionally `--location D:\WSL\Ubuntu-24.04` to keep it off C:).
  2. Create the Linux username/password when prompted. Do not share the password.
  3. Docker Desktop → *Settings → Resources → WSL integration* → enable **Ubuntu-24.04**.
- **Success signal:** `wsl -l -v` lists `Ubuntu-24.04` as version 2; `wsl -d Ubuntu-24.04 -- uname -a` succeeds.
- **Continues meanwhile:** Everything except Phase 3 Ansible spike and Phases 13–14.

## B4 — No isolated Ubuntu target node for Ansible — OPEN (user + teacher decision)

- **Missing:** No VM software is installed, and Windows 11 Home does not include Hyper-V Manager. RAM is limited (see B2).
- **Options:**
  - **A (plan's primary):** install VirtualBox and create an Ubuntu Server 24.04 LTS VM (about 2 GB RAM, 20 GB disk on D:), with a host-only/NAT port-forward for SSH. Heavier on RAM.
  - **B (plan's fallback):** a second, dedicated WSL2 distro (e.g. `Ubuntu-24.04` imported as `lastmile-target`) used only as the Ansible target, reached over SSH. Lighter, but the teacher must accept it as a "clean node".
- **Why user:** Choosing a target and confirming teacher acceptance; VM installation needs administrator approval.
- **Success signal:** From WSL Ubuntu, `ssh -i ~/.ssh/<key> <deploy-user>@<target> hostname` returns the target hostname without a password prompt.
- **Continues meanwhile:** All phases through 12; Ansible playbooks can be written and syntax-checked in Phase 13 before the target exists.

## B5 — GitHub CLI not authenticated — OPEN (human action required)

- **Update 2026-10-19:** `gh` 2.102.0 installed by Claude via `winget install --id GitHub.cli -e --scope user` (winget verified the installer hash). `gh auth status` → not logged in.
- **Missing (original):** `gh` is not installed, so issues and pull requests cannot be created from this session. Plain `git push` may work through Git Credential Manager (not yet tested).
- **Why user:** Installation may need approval; `gh auth login` is a browser OAuth flow the user must complete.
- **Steps (PowerShell, new terminal so PATH refreshes):** `gh auth login` → GitHub.com → HTTPS → *Login with a web browser*; answer **Yes** to "Authenticate Git with your GitHub credentials".
- **Success signal:** `gh auth status` shows the account as logged in to github.com.
- **Continues meanwhile:** Local branches and commits; pushes once credentials work. PR creation (Phase 5/6) waits for this.

## B6 — Teacher-specific clarifications — OPEN (user asks teacher)

Answers change acceptance evidence, so they are recorded rather than assumed:

1. Is a **local** deployment URL (e.g. `http://localhost:8088`) plus screenshots acceptable, or must the app be publicly reachable? (Plan default: local.)
2. Does the PR in Phase 5 need review by a **real second person** (classmate/teacher)? Claude cannot supply one.
3. If no VM is possible, is an isolated **WSL2 distro** acceptable as the Ansible target? (See B4.)
4. Is minimal **admin user management** required, or are seeded accounts with role enforcement enough? (Plan default: seeded accounts plus a read-only admin user list.)
5. Is a Dockerized Spring Boot JAR behind Nginx acceptable, or is an external Tomcat expected? (Plan default: Dockerized JAR behind Nginx.)

- **Continues meanwhile:** Everything; defaults above are used and recorded in `docs/DECISIONS_LOG.md` until answered.
