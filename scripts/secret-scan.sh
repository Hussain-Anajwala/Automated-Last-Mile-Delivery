#!/usr/bin/env bash
# Secret scan gate (fails closed). Exit 0 only when the scanner ran successfully and found nothing.
#   scripts/secret-scan.sh staged    # staged changes (before commit)
#   scripts/secret-scan.sh history   # all commits (before push; used by scripts/safe-push.sh)
#   scripts/secret-scan.sh tree      # working-tree files Git could publish
# Exit codes: 0 clean | 1 findings | 2 usage error | 3 scanner missing or failed to run.
# Do not pipe this into another command when using it as a gate: a pipeline takes the exit status
# of its last command (this mistake let a scan "pass" on 2026-10-19; see IMPLEMENTATION_LOG).
set -uo pipefail

if [ -n "${GITLEAKS:-}" ]; then
  # Explicit choice: use exactly this scanner; never fall back silently.
  command -v "$GITLEAKS" >/dev/null 2>&1 || { echo "SECRET SCAN ERROR: GITLEAKS=$GITLEAKS not found" >&2; exit 3; }
elif command -v gitleaks >/dev/null 2>&1; then
  GITLEAKS=gitleaks
else
  GITLEAKS="${LOCALAPPDATA:-}/Microsoft/WinGet/Packages/Gitleaks.Gitleaks_Microsoft.Winget.Source_8wekyb3d8bbwe/gitleaks.exe"
  [ -x "$GITLEAKS" ] || { echo "SECRET SCAN ERROR: gitleaks not installed" >&2; exit 3; }
fi
[ -f .gitleaks.toml ] || { echo "SECRET SCAN ERROR: run from the repository root (.gitleaks.toml missing)" >&2; exit 3; }

mode="${1:-staged}"
case "$mode" in
  staged)  args=(git --staged .) ;;
  history) args=(git .) ;;
  tree)    args=(dir .) ;;
  *) echo "usage: $0 staged|history|tree" >&2; exit 2 ;;
esac

report="$(mktemp)"
trap 'rm -f "$report"' EXIT
# --exit-code 1 on findings; any other non-zero status means the scanner itself failed.
"$GITLEAKS" "${args[@]}" --no-banner --redact --config .gitleaks.toml --exit-code 1 \
  --report-format csv --report-path "$report"
status=$?
if [ $status -ne 0 ] && [ $status -ne 1 ]; then
  echo "SECRET SCAN ERROR ($mode): scanner exited with $status" >&2
  exit 3
fi

rc=0
if [ $status -eq 1 ]; then
  if [ "$mode" = tree ]; then
    # Findings in git-ignored files (e.g. ci/jenkins/.env, which legitimately holds local secrets)
    # are listed but cannot be published; anything else fails.
    while IFS=, read -r _rule _commit file _rest; do
      [ -z "$file" ] && continue
      if git check-ignore -q -- "$file"; then
        echo "ignored by git (not publishable): $file"
      else
        echo "FINDING in publishable file: $file"
        rc=1
      fi
    done < <(tail -n +2 "$report" | cut -d, -f1-3 | sort -u)
  else
    rc=1
  fi
fi

if [ $rc -ne 0 ]; then
  echo "SECRET SCAN FAILED ($mode): review the findings above before committing or pushing." >&2
else
  echo "secret scan passed ($mode)"
fi
exit $rc
