# Condition evaluation

Each condition evaluates to `TRUE`, `FALSE`, or `PENDING`.

## Capsule outcome (AND)

```text
IF any FALSE  → remain LOCKED
IF any PENDING → WAITING (not unlockable yet)
IF all TRUE   → UNLOCKABLE
```

## Capsule outcome (OR)

```text
IF any TRUE   → UNLOCKABLE
IF all FALSE  → LOCKED
ELSE          → WAITING
```

## Date (UTC)

```text
currentTime >= unlockAt → TRUE
else                    → FALSE
```

## Identity

```text
authenticatedUserId == recipientId → TRUE
authenticatedUserId missing          → PENDING
else                               → FALSE
```

## Location (Haversine)

Server compares reported `(lat, lon)` to config center and `radiusMeters`.

## Scheduler

Every 30s (configurable): find `LOCKED` capsules where `next_evaluation_at <= now()`, run evaluation with server context (date conditions; identity/location remain pending until client supplies auth/location).
