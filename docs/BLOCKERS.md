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

## B2 — Docker Desktop not running; limited RAM and C: disk — RESOLVED 2026-10-19 (user started Docker; data on D:)

- **Resolution:** `docker run --rm hello-world` → `Hello from Docker!` (exit 0). Docker Desktop data is at `D:\Docker Data`, WSL distros at `D:\WSL`. Memory pressure remains — see B7.
- **Missing (original):** Docker daemon is unreachable (`dockerDesktopLinuxEngine` pipe absent). Free RAM was 0.6 GB of 15.8 GB; C: has 9.7 GB free. Docker images, Jenkins, PostgreSQL and a browser will not fit on C: comfortably.
- **Why user:** Starting Docker Desktop, accepting its terms and changing its storage settings are GUI actions on the user's machine.
- **Steps (Windows):**
  1. Close unneeded browser windows before working sessions (several Brave/Chrome processes were using ~3 GB).
  2. Start **Docker Desktop** from the Start menu and wait until it shows *Engine running*.
  3. Recommended: *Settings → Resources → Advanced → Disk image location* → choose a folder on `D:` (for example `D:\DockerDesktop`), then *Apply & restart*.
  4. Recommended: *Settings → Resources* → limit memory to about 6–8 GB if the option is shown (otherwise create `%UserProfile%\.wslconfig` with `[wsl2]` / `memory=8GB` and run `wsl --shutdown`).
- **Success signal (PowerShell):** `docker version` shows a Server section and `docker run --rm hello-world` prints `Hello from Docker!`.
- **Continues meanwhile:** Phases 1–2; Maven/JUnit work once B1 is resolved.

## B3 — No Ubuntu WSL2 distribution (Ansible control node) — RESOLVED 2026-10-19

- **Resolution:** User installed `Ubuntu-24.04` (WSL2, on D:). Docker works inside it. `sudo` needs the user's password, so Claude installed `ansible-core 2.21.5` into `~/.venvs/ansible` without sudo (`scripts/bootstrap-ansible-wsl.sh`).
- **Missing (original):** Only the `docker-desktop` WSL distro exists. Ansible must run from Ubuntu under WSL2.
- **Why user:** Installing a distro creates a Linux user account and password interactively.
- **Steps (PowerShell):**
  1. `wsl --install -d Ubuntu-24.04` (optionally `--location D:\WSL\Ubuntu-24.04` to keep it off C:).
  2. Create the Linux username/password when prompted. Do not share the password.
  3. Docker Desktop → *Settings → Resources → WSL integration* → enable **Ubuntu-24.04**.
- **Success signal:** `wsl -l -v` lists `Ubuntu-24.04` as version 2; `wsl -d Ubuntu-24.04 -- uname -a` succeeds.
- **Continues meanwhile:** Everything except Phase 3 Ansible spike and Phases 13–14.

## B4 — No isolated Ubuntu target node for Ansible — OPEN (user decision + approval; teacher acceptance)

- **Status 2026-10-19:** Everything that does not need the target is done: roles written, `--syntax-check` passes, `ansible-lint` passes (production profile), control node ready. Phases 13 (execution) and 14 wait for a target.
- **Facts checked:** WSL 2.6.3; `VirtualMachinePlatform` and `HypervisorPlatform` enabled (VirtualBox can coexist with Hyper-V); no VM software installed; free commit memory 0.5–3.9 GB during work (B7).
- **Option A — VirtualBox VM (plan's primary):** Ubuntu Server 24.04, 2 vCPU / 2 GB RAM / 20 GB disk on D:, host-only adapter (registry published only on that host-only interface). Strongest "separate node" story. Needs: VirtualBox install (admin prompt), ~2 GB RAM free (close Brave first), Ubuntu install with an SSH key.
- **Option B — dedicated WSL2 distro `lastmile-target` (plan's fallback, recommended on this machine's memory):** separate root filesystem, own systemd, sshd and Docker Engine; created by Claude with `wsl --import` (no admin) and removable with `wsl --unregister lastmile-target`. Limits: shares the WSL kernel/network stack with Docker Desktop, so ufw stays off on this target (`base_manage_firewall: false`) and the registry must be reachable from WSL. **Needs teacher acceptance as a "clean node".**
- **What Claude needs:** a reply choosing A or B. For B, also explicit approval to add a WSL distro (it changes WSL configuration). For A, install VirtualBox and create the VM, or ask Claude for exact click-by-click steps.
- **Success signal:** `bash scripts/ansible-run.sh ansible -m ansible.builtin.ping lastmile_targets` → `pong`.

## B5 — GitHub CLI not authenticated — RESOLVED 2026-10-19

- **Resolution:** The user authenticated. Verified in both Git Bash and PowerShell with the same executable (`…\WinGet\Packages\GitHub.cli_…in\gh.exe`): logged in as `Hussain-Anajwala`, scopes `repo`, `workflow`. The earlier "not logged in" result came from this session's stale PATH plus the state before login. Issues #1–#7 and PRs #8–#15 created.
- **Update 2026-10-19:** `gh` 2.102.0 installed by Claude via `winget install --id GitHub.cli -e --scope user` (winget verified the installer hash). `gh auth status` → not logged in.
- **Missing (original):** `gh` is not installed, so issues and pull requests cannot be created from this session. Plain `git push` may work through Git Credential Manager (not yet tested).
- **Why user:** Installation may need approval; `gh auth login` is a browser OAuth flow the user must complete.
- **Steps (PowerShell, new terminal so PATH refreshes):** `gh auth login` → GitHub.com → HTTPS → *Login with a web browser*; answer **Yes** to "Authenticate Git with your GitHub credentials".
- **Success signal:** `gh auth status` shows the account as logged in to github.com.
- **Continues meanwhile:** Local branches and commits; pushes once credentials work. PR creation (Phase 5/6) waits for this.

## B6 — Teacher questions (single list) — OPEN (user asks the teacher)

Provisional defaults are used until answered; none of them is treated as teacher approval.

| # | Question | Provisional default in use | Affects |
|---|---|---|---|
| Q1 | Is a local URL (`http://127.0.0.1:8088`) plus screenshots/video acceptable, or is public hosting required? | Local only | Tasks 8, 15 |
| Q2 | Must a real second person review pull requests? | Documented self-review (D15) | Tasks 5, 6 |
| Q3 | Is a dedicated WSL2 distribution acceptable as the "clean" Ansible target, or must it be a VM? | Recommend WSL2 (B4) — not created yet | Tasks 13, 14 |
| Q4 | Is a read-only admin user list enough, or is full user management required? | Read-only list | Task 6 |
| Q5 | Is a Dockerized Spring Boot app behind Nginx acceptable, or is external Tomcat mandatory? | Dockerized JAR + Nginx | Tasks 8, 11 |

## B7 — Memory pressure on the workstation — OPEN (measured; user decision on .wslconfig)

- **Observed:** two WSL VM crashes during Jenkins image builds (commit limit exhausted). Since then: one Jenkins executor (D17), heavy jobs run one at a time, memory sampled during runs.
- **Measured during the `main` pipeline (2026-10-19 07:31–07:33 IST, Selenium stage):** Jenkins container peak 2.2 GiB (limit 3 GiB); app stack ~0.35 GiB; WSL VM `used` peak 2.8 GiB plus ~4.2 GiB page cache; host `vmmemWSL` 3.2 GB; host free commit fell to **1.1 GB**. Largest host consumers at the same time: Brave ~2.8 GB, VS Code ~2.1 GB.
- **Proposal (needs your approval; not applied):** `%UserProfile%\.wslconfig` with `[wsl2]` `memory=6GB` and `[experimental]` `autoMemoryReclaim=gradual`.
  - Expected effect: WSL can no longer grow past 6 GB, and idle page cache is returned to Windows gradually. The measured workload (~2.8 GB used) fits with room for the 3 GiB Jenkins limit plus the app stack.
  - Trade-off: less file cache inside WSL (slower repeated builds); if a future workload needs more than 6 GB it would hit the cap. Applying it requires `wsl --shutdown`, which stops Docker Desktop, Jenkins, the registry and the app until restarted (volumes are kept).
  - Verify: after restart, `wsl -d Ubuntu-24.04 -- free -m` total ≈ 6 GB; rerun one pipeline while sampling host free commit (should stay higher than 1.1 GB).
- **Most effective without any change:** close Brave/other heavy apps during pipeline runs.

