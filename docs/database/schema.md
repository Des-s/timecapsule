# Database schema

PostgreSQL with Flyway migration `V1__init.sql`.

## Core tables

### `users`

| Column | Notes |
|--------|--------|
| `id` | UUID PK |
| `email` | unique |
| `password_hash` | bcrypt |
| `display_name` | |
| `email_verified` | default false |

### `capsules`

| Column | Notes |
|--------|--------|
| `creator_id`, `recipient_id` | FK → users |
| `encrypted_content` | ciphertext (plaintext placeholder until client crypto) |
| `status` | DRAFT, LOCKED, UNLOCKING, UNLOCKED, EXPIRED, CANCELLED |
| `logic_operator` | AND (default), OR |
| `next_evaluation_at` | indexed for scheduler |

### `conditions`

| Column | Notes |
|--------|--------|
| `type` | DATE, LOCATION, IDENTITY, CONNECTIVITY |
| `config` | JSONB (`unlockAt`, lat/lon/radius, etc.) |
| `status` | PENDING, TRUE, FALSE |

### `capsule_events`

Append-only audit log: CREATED, LOCKED, CONDITION_EVALUATED, UNLOCKED, OPENED, …

### `processed_operations`

Reserved for offline sync idempotency (mobile phase).
