# Merge-conflict demonstration transcript (teacher task 6) — 2026-10-19

Disposable branches `demo/conflict-base`, `demo/conflict-a`, `demo/conflict-b` (pushed to origin), one harmless
documentation line in `docs/demo/conflict-demo.md`. No source, binary or generated file involved.
Commands and real output follow. `grep -c` printing `0` with exit 1 means no conflict markers remain.

```text
$ git switch -c demo/conflict-base develop
Switched to a new branch 'demo/conflict-base'
[exit 0]

$ git add docs/demo/conflict-demo.md
[exit 0]

$ git commit -m docs: add conflict demonstration file

Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>
[demo/conflict-base 4f38289] docs: add conflict demonstration file
 1 file changed, 5 insertions(+)
 create mode 100644 docs/demo/conflict-demo.md
[exit 0]

$ git switch -c demo/conflict-a
Switched to a new branch 'demo/conflict-a'
[exit 0]

$ git commit -am docs: set weekday support hours (branch a)

Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>
[demo/conflict-a 67c8d06] docs: set weekday support hours (branch a)
 1 file changed, 1 insertion(+), 1 deletion(-)
[exit 0]

$ git switch demo/conflict-base
Switched to branch 'demo/conflict-base'
[exit 0]

$ git switch -c demo/conflict-b
Switched to a new branch 'demo/conflict-b'
[exit 0]

$ git commit -am docs: set extended support hours (branch b)

Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>
[demo/conflict-b d7369e2] docs: set extended support hours (branch b)
 1 file changed, 1 insertion(+), 1 deletion(-)
[exit 0]

$ git switch demo/conflict-base
Switched to branch 'demo/conflict-base'
[exit 0]

$ git merge --no-ff demo/conflict-a -m docs: merge demo/conflict-a

Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>
Merge made by the 'ort' strategy.
 docs/demo/conflict-demo.md | 2 +-
 1 file changed, 1 insertion(+), 1 deletion(-)
[exit 0]

$ git merge --no-ff demo/conflict-b
Auto-merging docs/demo/conflict-demo.md
CONFLICT (content): Merge conflict in docs/demo/conflict-demo.md
Automatic merge failed; fix conflicts and then commit the result.
[exit 1]

$ git status --short
UU docs/demo/conflict-demo.md
[exit 0]

$ cat docs/demo/conflict-demo.md   # conflict markers
# Conflict demonstration

This file exists only to demonstrate merge-conflict resolution (teacher task 6).

<<<<<<< HEAD
Support hours: 09:00-18:00 IST, Monday to Friday
=======
Support hours: 08:00-20:00 IST, Monday to Saturday
>>>>>>> demo/conflict-b

# Manual resolution: edited the file, removed markers, kept branch b hours (covers branch a window)
$ grep -c <<<<<<<\|>>>>>>>\|======= docs/demo/conflict-demo.md
0
[exit 1]

$ git add docs/demo/conflict-demo.md
[exit 0]

$ git commit -m docs: resolve support-hours conflict between demo branches

Both sides changed the same line. Kept branch b (08:00-20:00 Mon-Sat)
because it fully covers branch a's 09:00-18:00 Mon-Fri window.

Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>
[demo/conflict-base cbe5031] docs: resolve support-hours conflict between demo branches
[exit 0]

$ git log --graph --oneline -6
*   cbe5031 docs: resolve support-hours conflict between demo branches
|\  
| * d7369e2 docs: set extended support hours (branch b)
* |   a78e069 docs: merge demo/conflict-a
|\ \  
| |/  
|/|   
| * 67c8d06 docs: set weekday support hours (branch a)
|/  
* 4f38289 docs: add conflict demonstration file
*   13af8d2 docs: merge phase 4 log
|\  
[exit 0]

$ cat docs/demo/conflict-demo.md   # resolved
# Conflict demonstration

This file exists only to demonstrate merge-conflict resolution (teacher task 6).

Support hours: 08:00-20:00 IST, Monday to Saturday

Resolution note: branch a proposed 09:00-18:00 Mon-Fri and branch b 08:00-20:00 Mon-Sat on the same
line. Both sides were inspected; b was kept because its window fully covers a's, so no committed
support time is lost.
```
