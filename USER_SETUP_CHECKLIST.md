# User Setup Checklist — Claude Code Project

Claude Code should inspect the machine and confirm what is already available before asking you to install anything. Do not install duplicate tools just because they appear in this checklist.

## 1. Recommended local setup

- [ ] Windows 10/11 machine with sufficient free disk space; 16 GB RAM is recommended for Docker + Jenkins + database + browser tests + an Ubuntu target. If hardware is more limited, have Claude select a lighter topology and document the limitations.
- [ ] WSL2 enabled and an Ubuntu LTS distribution installed.
- [ ] Docker Desktop installed, running, and configured to use the WSL2 backend with integration enabled for the selected Ubuntu distribution.
- [ ] Git installed and configured with your preferred name/email.
- [ ] A supported Node.js LTS version and Claude Code installed in the chosen environment. Running Claude Code inside Ubuntu/WSL2 is recommended for a consistent Bash/Ansible toolchain.
- [ ] Claude Code authenticated through the approved Claude account/subscription or Anthropic Console sign-in flow.
- [ ] JDK 21 available in the environment where Maven will run. Prefer the project's Maven Wrapper rather than a global Maven installation.
- [ ] A modern browser such as Chrome or Edge installed if Selenium is not using a managed/downloadable browser in headless CI.
- [ ] Optional but useful: GitHub CLI (`gh`) installed and authenticated with `gh auth login`.

Official Claude Code references:

- [Claude Code setup and installation](https://docs.anthropic.com/en/docs/claude-code/getting-started)
- [Claude Code CLI usage and permission modes](https://docs.anthropic.com/en/docs/claude-code/cli-usage)
- [Anthropic's Claude Code best practices](https://www.anthropic.com/engineering/claude-code-best-practices)

Use official product installation guides and your machine's actual settings. Claude Code may be able to inspect versions and install project-local dependencies, but Windows security prompts, admin elevation, enabling virtualization, WSL installation and reboots require you.

## 2. GitHub access

- [ ] Decide whether the repository should be **public** (convenient for a resume and teacher access after secret scanning) or **private** (you may need to invite the teacher/reviewer).
- [ ] Either create an empty GitHub repository yourself or authenticate `gh` so Claude Code can create/manage it after the visibility choice is known.
- [ ] Never paste GitHub tokens into Claude prompts. Authenticate through `gh auth login` or the official browser flow.
- [ ] If the course requires an independent pull-request review, arrange for a classmate/teacher to review the PR. Claude Code can self-review code but cannot truthfully manufacture a second person's approval.

The local app does not require a third-party business API key. The expected external authentication is primarily your Claude account and, if using GitHub operations, your GitHub account.

## 3. Docker and registry

- [ ] Run `docker version` and `docker compose version` in the environment where Claude Code will work.
- [ ] Start Docker Desktop and confirm that a simple test container can run, if not already verified.
- [ ] Keep the local image registry restricted to the local development environment/trusted interface; do not expose it to the public internet.
- [ ] Permit Claude Code to inspect Docker state, but review requests that alter Docker daemon configuration or mount the Docker socket into Jenkins. Docker socket access is highly privileged.

No Docker Hub account should be required for the planned local-registry workflow.

## 4. Ansible target

The recommended target is a dedicated local Ubuntu LTS VM reachable over SSH. This is separate from the Windows host and gives the provisioning exercise a cleaner “target node.”

- [ ] Check that virtualization is enabled and a local VM solution is available.
- [ ] Create/start the isolated Ubuntu VM if Claude Code cannot do this on your machine.
- [ ] Create a non-root deployment user with narrowly scoped sudo permissions as needed.
- [ ] Configure SSH access (prefer an SSH key); do not paste the private key into chat or commit it.
- [ ] Keep the VM address and non-secret connection details in a local gitignored config file or your SSH config.
- [ ] Verify SSH connectivity from Ubuntu/WSL2 before Phase 13/14.

If you cannot create a VM, ask the teacher whether an isolated WSL2 Ubuntu target is accepted. Do not point Ansible at your everyday Windows machine or an unrelated server.

## 5. Jenkins and local network

- [ ] Ensure enough RAM/disk exists to run the chosen Jenkins topology alongside Docker, PostgreSQL and Selenium.
- [ ] Allow local ports only as needed. Claude Code should check for collisions before choosing ports.
- [ ] Use Jenkins SCM polling for a local setup unless an authenticated, secure webhook is explicitly required. Do not expose localhost Jenkins through an untrusted tunnel just to receive GitHub webhooks.
- [ ] If Jenkins setup opens a first-run unlock screen or requires a login you must complete, Claude Code should tell you exactly what to click/run and then resume.

## 6. Actions Claude Code generally cannot do without you

1. Approve system-level Windows prompts, enable virtualization/features, reboot, or complete interactive software installation where admin rights are required.
2. Sign in to Claude/GitHub in a browser or approve OAuth flows; provide credentials to a local credential manager, not to chat.
3. Decide repository visibility and invite an actual teacher/classmate/reviewer when required.
4. Create a VM or network configuration when the machine lacks virtualization/permissions, or confirm the VM's SSH identity/fingerprint if prompted.
5. Answer genuine teacher-specific requirements not included in the assignment (for example, if the teacher later mandates a particular Jenkins topology or an externally reachable URL).
6. Record or submit your personal viva/video/presentation, give a real live demonstration, and confirm that the course evaluator can access the repository.

Claude Code should do the technical work around these steps, produce precise instructions, and continue the rest of the project wherever possible. Do not give Claude passwords or private keys in plain text.

## 7. Verify before starting

Start Claude Code at the repository root and let it run the audit first. It should report actual detection results, not assume that each prerequisite is missing. Then paste or invoke `MASTER_PROMPT_CLAUDE_CODE.md`. If it reports a blocker, follow the exact local steps and relaunch/resume the same Claude Code session.
