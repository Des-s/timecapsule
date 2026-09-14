# Architecture overview

```text
                  ┌───────────────────────┐
                  │     React Native      │
                  │        + Expo         │
                  └───────────┬───────────┘
                              │ REST
                              ▼
                  ┌───────────────────────┐
                  │      Spring Boot      │
                  │  Auth · Capsules      │
                  │  Conditions · Evaluator │
                  │  Audit · Notifications│
                  └───────────┬───────────┘
                              │
                              ▼
                         PostgreSQL
                              │
                              ▼
                    @Scheduled evaluator
                              │
                 ┌────────────┼────────────┐
                 ▼            ▼            ▼
               DATE       LOCATION     IDENTITY
```

## Principles

- **Modular monolith** for MVP (no microservices).
- **Server is authority** for unlock decisions; clients submit evidence (e.g. location readings).
- **UTC internally**, local time in UI.
- **State machine** for capsule lifecycle (`DRAFT` → `LOCKED` → `UNLOCKED`; no revert after unlock).
- **Audit events** for portfolio demos and debugging.

## Packages (backend)

| Module | Responsibility |
|--------|----------------|
| `auth` | Users, JWT, registration/login |
| `capsule` | CRUD, lock, open, content access rules |
| `condition` | Condition CRUD, location-check ingestion |
| `evaluation` | Evaluators, scheduler, unlock orchestration |
| `audit` | Append-only capsule timeline |
