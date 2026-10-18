# Git Workflow, Branch and Pull-Request Policy

## Branches

| Branch | Purpose | Merges into |
|---|---|---|
| `main` | Reviewed, release-ready baseline; release tags live here | — |
| `develop` | Integration branch for features (created in Phase 4) | `main` via release PR |
| `feature/<short-name>` | Product features, e.g. `feature/delivery-create` | `develop` |
| `fix/<short-name>` | Defect fixes | `develop` (or `main` for hotfix) |
| `test/<short-name>` | Test suites, e.g. `test/selenium-smoke` | `develop` |
| `ci/<short-name>` | Jenkins/pipeline, e.g. `ci/jenkins-pipeline` | `develop` |
| `infra/<short-name>` | Docker/Ansible, e.g. `infra/ansible-provisioning` | `develop` |
| `docs/<short-name>` | Documentation only | `develop` (`main` before `develop` exists) |
| `release/<version>` | Optional release stabilisation | `main` and back to `develop` |

Phases 0–2 were documentation-only and were merged to `main` with `--no-ff` before `develop` existed.

## Commits

`<type>: <imperative summary>` with types `feat`, `fix`, `test`, `docs`, `ci`, `build`, `infra`,
`refactor`, `chore`. One logical change per commit. No `wip`, `final`, `changes`.

## Pull requests

- Every feature/fix/ci/infra branch is merged through a GitHub PR once `gh` is authenticated (B5).
- PR description: change summary, linked issue, tests run with results, risks, screenshots/logs.
- Merge only with a passing build (`mvnw verify`; Jenkins once available). Use merge commits (no squash) so branch history stays visible as evidence.
- **Reviews:** Claude performs and records the self-review checklist below in the PR. Claude never poses as a second reviewer or writes approval on someone else's behalf. If the teacher requires an independent reviewer (B6.2), the user invites one and the PR waits for that real review.

### Self-review checklist (copied into each PR)

- [ ] Scope matches the linked issue; no unrelated changes
- [ ] Authorization enforced server-side; client cannot set system fields
- [ ] Validation and error states handled
- [ ] Tests added/updated and passing (list commands + counts)
- [ ] No secrets, generated files or local config in diff
- [ ] Docs updated

## Conflict-resolution demonstration (Task 6)

Performed on two disposable branches (`demo/conflict-a`, `demo/conflict-b`) editing one harmless
line in `docs/conflict-demo.md`. Both sides are inspected, resolved by hand, and the full transcript
(`git merge` output, conflict markers, resolution, `git log --graph`) is saved under
`docs/evidence/phase-06-*`. No source, binary or generated file is used.

## Tags and releases

Annotated semantic tags (`v0.1.0` MVP baseline, `v1.0.0` final) on `main`, only after the
required tests pass. Docker images are tagged with the short commit SHA plus the release tag.

## Prohibited without explicit owner approval

Force push, history rewrite of shared branches, deleting remote branches, `git reset --hard` or
`git clean` to discard work, changing repository visibility/settings.
