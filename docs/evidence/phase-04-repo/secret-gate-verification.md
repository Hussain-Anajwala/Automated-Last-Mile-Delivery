# Secret-scan gate fail-closed verification (2026-10-19)

Command: `bash scripts/test-secret-gate.sh` (throwaway repository + local bare remote in a temp dir;
simulated keys are random and never enter the project history). Exit code: 0.

```text
[OK]   clean staged -> exit 0
[OK]   simulated finding staged -> exit 1
[OK]   simulated finding in history -> exit 1
[OK]   safe-push blocked by finding -> exit 1
[OK]   scanner crash fails closed -> exit 3
[OK]   missing scanner fails closed -> exit 3
[OK]   safe-push blocked by scanner crash -> exit 1
[OK]   remote untouched by blocked pushes
[OK]   tree: finding only in git-ignored file -> exit 0
[OK]   tree: finding in publishable file -> exit 1
failures: 0
```

Real repository at the time: `secret-scan.sh history` → exit 0; `secret-scan.sh tree` → exit 0
(only finding: git-ignored `ci/jenkins/.env`, which is not publishable).

Fail-open paths found and fixed while preparing this test: an explicitly set but missing
`GITLEAKS` silently fell back to another binary, and in `tree` mode a crashed or missing scanner
produced an empty report that was read as "no findings". Both now exit 3.
