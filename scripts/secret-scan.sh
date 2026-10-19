#!/usr/bin/env bash
# Secret scan gate. Exits non-zero if gitleaks finds anything, so it can guard commits and pushes:
#   scripts/secret-scan.sh staged    # staged changes (before commit)
#   scripts/secret-scan.sh history   # all commits (before push)
#   scripts/secret-scan.sh tree      # working tree files
# Do not pipe its output into another command when using it as a gate: a pipeline takes the exit
# status of the last command (this mistake let a scan "pass" on 2026-10-19; see IMPLEMENTATION_LOG).
set -uo pipefail
GITLEAKS="${GITLEAKS:-gitleaks}"
if ! command -v "$GITLEAKS" >/dev/null 2>&1; then
  for candidate in "$LOCALAPPDATA/Microsoft/WinGet/Packages/Gitleaks.Gitleaks_Microsoft.Winget.Source_8wekyb3d8bbwe/gitleaks.exe"; do
    [ -x "$candidate" ] && GITLEAKS="$candidate"
  done
fi
mode="${1:-staged}"
case "$mode" in
  staged)  "$GITLEAKS" git --no-banner --redact --config .gitleaks.toml --staged . ;;
  history) "$GITLEAKS" git --no-banner --redact --config .gitleaks.toml . ;;
  tree)
    # Working tree, but only files Git could publish: findings in git-ignored files (e.g. the local
    # ci/jenkins/.env that legitimately holds the Jenkins password) are listed, not failed on.
    report="$(mktemp)"
    "$GITLEAKS" dir --no-banner --redact --config .gitleaks.toml --report-format csv --report-path "$report" . >/dev/null 2>&1
    rc=0
    while IFS=, read -r _rule _commit file _rest; do
      [ -z "$file" ] && continue
      if git check-ignore -q -- "$file"; then
        echo "ignored by git (not publishable): $file"
      else
        echo "FINDING in publishable file: $file"
        rc=1
      fi
    done < <(tail -n +2 "$report" | cut -d, -f1-3 | sort -u)
    rm -f "$report"
    [ $rc -eq 0 ] && echo "no leaks in publishable files"
    ;;
  *) echo "usage: $0 staged|history|tree" >&2; exit 2 ;;
esac
rc=${rc:-$?}
if [ $rc -ne 0 ]; then
  echo "SECRET SCAN FAILED ($mode): review the findings above before committing or pushing." >&2
fi
exit $rc
