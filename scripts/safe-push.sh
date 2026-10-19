#!/usr/bin/env bash
# Push only after the full-history secret scan passes (fails closed).
# Usage: scripts/safe-push.sh [git push arguments...]     e.g. scripts/safe-push.sh origin develop
# Any scanner finding, scanner error or missing scanner blocks the push. Never bypass it.
set -uo pipefail
here="$(cd "$(dirname "$0")" && pwd)"
if ! bash "$here/secret-scan.sh" history; then
  echo "PUSH BLOCKED: secret scan did not pass (findings, scanner error or scanner missing)." >&2
  exit 1
fi
exec git push "$@"
