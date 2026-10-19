#!/usr/bin/env bash
# Proves scripts/secret-scan.sh and scripts/safe-push.sh fail closed. Runs in a throwaway repository
# under a temporary directory with a local bare "remote"; simulated keys are random and never touch
# the project's history. Usage (repository root, Git Bash or Ubuntu/WSL): bash scripts/test-secret-gate.sh
set -uo pipefail
REPO="$(cd "$(dirname "$0")/.." && pwd)"
WORK="$(mktemp -d)"; trap 'rm -rf "$WORK"' EXIT
git init -q --bare "$WORK/remote.git"
mkdir -p "$WORK/repo/scripts" && cd "$WORK/repo" && git init -q
git config user.email gate@example.invalid; git config user.name gate-test
cp "$REPO/.gitleaks.toml" .; cp "$REPO/scripts/secret-scan.sh" "$REPO/scripts/safe-push.sh" scripts/
printf '.env\n' > .gitignore; echo hello > readme.txt; git add -A; git commit -qm init
git remote add origin "$WORK/remote.git"
printf '#!/bin/sh\nexit 2\n' > "$WORK/crash"; chmod +x "$WORK/crash"
fake() { echo "api_key = \"$(head -c 20 /dev/urandom | od -An -tx1 | tr -d ' \n')\""; }
failures=0
t() { local desc="$1" expect="$2"; shift 2; "$@" >/dev/null 2>&1; local rc=$?
  if [ "$rc" = "$expect" ]; then echo "[OK]   $desc -> exit $rc"; else echo "[FAIL] $desc -> exit $rc (expected $expect)"; failures=$((failures+1)); fi; }
t "clean staged" 0 bash scripts/secret-scan.sh staged
fake > leak.txt; git add leak.txt
t "simulated finding staged" 1 bash scripts/secret-scan.sh staged
git commit -qm "simulated leak"
t "simulated finding in history" 1 bash scripts/secret-scan.sh history
t "safe-push blocked by finding" 1 bash scripts/safe-push.sh -q origin HEAD:refs/heads/main
t "scanner crash fails closed" 3 env GITLEAKS="$WORK/crash" bash scripts/secret-scan.sh tree
t "missing scanner fails closed" 3 env GITLEAKS=/nonexistent/gitleaks bash scripts/secret-scan.sh history
t "safe-push blocked by scanner crash" 1 env GITLEAKS="$WORK/crash" bash scripts/safe-push.sh -q origin HEAD:refs/heads/main
refs=$(git ls-remote "$WORK/remote.git" | wc -l)
if [ "$refs" -eq 0 ]; then echo "[OK]   remote untouched by blocked pushes"; else echo "[FAIL] remote has $refs refs"; failures=$((failures+1)); fi
git rm -q --cached leak.txt; git commit -qm "untrack"; mv leak.txt .env
t "tree: finding only in git-ignored file" 0 bash scripts/secret-scan.sh tree
fake > publishable.txt
t "tree: finding in publishable file" 1 bash scripts/secret-scan.sh tree
echo "failures: $failures"
exit $((failures > 0))
