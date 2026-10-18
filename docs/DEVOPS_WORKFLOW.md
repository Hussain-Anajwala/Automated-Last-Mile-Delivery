# Development-to-Operations Workflow (Teacher Task 2)

```mermaid
flowchart LR
    subgraph Plan
        A[Backlog ticket<br/>docs/project-board.md] --> B[User story +<br/>acceptance criteria]
    end
    subgraph Code
        B --> C[feature/* branch]
        C --> D[Code + unit/integration tests<br/>mvnw verify]
        D --> E[Commit + push]
        E --> F[Pull request<br/>self-review checklist]
        F -->|approved, checks green| G[Merge to develop/main]
    end
    subgraph CI["CI — Jenkins (SCM polling)"]
        G --> H[Checkout + record SHA]
        H --> I[Maven build + JUnit]
        I -->|fail| X[Stop: publish reports,<br/>no deploy]
        I --> J[Package JAR + archive]
        J --> K[Test stack + Selenium headless]
        K -->|fail| X
    end
    subgraph CD["CD — Docker"]
        K --> L[Build image<br/>tag = short SHA]
        L --> M[Push to local registry]
        M --> N[Deploy by tag<br/>Nginx → app → PostgreSQL]
        N --> O{Readiness 200?}
        O -->|no| P[Rollback to last<br/>known-good tag]
        O -->|yes| Q[Record release]
    end
    subgraph Ops["Ops — Ansible"]
        R[Playbook: packages, user,<br/>dirs, config, services] --> N
        Q --> S[Re-run playbook:<br/>changed=0]
        P --> T[Verify health after recovery]
    end
    Q --> U[Tag release + evidence]
```

## Evidence produced at each stage

| Stage | Evidence (stored under `docs/evidence/`) | Task |
|---|---|---|
| Plan | Board, stories, DoD | T1–T2 |
| Code | Branch graph, commits, PR, conflict transcript, tag | T4–T6 |
| CI | Jenkins console logs, poll log, archived JAR, JUnit report | T7 |
| Pipeline | Jenkinsfile stage view, parameter value, health output | T8 |
| Selenium | Test plan, report, failure screenshot | T9–T10 |
| Docker | Lifecycle transcript, `docker inspect`, image ID/digest | T11 |
| Registry/CD | Catalog/tags, pull log, deployed version = SHA | T12 |
| Ansible | Run 1 and run 2 recaps | T13–T14 |
| Recovery | Failed candidate, rollback, health result | T14 |
| Release | `docs/evidence/INDEX.md` | T15 |
