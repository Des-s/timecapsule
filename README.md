# TimeCapsule

Secure mobile capsules that stay locked until **time**, **place**, and **identity** conditions are satisfied.

This repository is structured for a portfolio-first build that can grow into a product:

```text
timecapsule/
├── backend/     Spring Boot 4 + PostgreSQL (condition engine authority)
├── mobile/      Expo / React Native (planned)
└── docs/        Architecture, API, database, algorithms
```

## MVP definition (v1.0)

Authenticated users create encrypted capsules for themselves or other users, attach conditions, lock them, and the backend evaluates conditions, unlocks when satisfied, writes audit events, and exposes content only after unlock.

**Current backend milestone:** auth, capsule CRUD, lock/open, date/identity/location conditions, scheduled date evaluation, audit timeline, location check endpoint.

**Mobile (Expo):** auth, capsule list/create, condition builder, lock/open, foreground location check, audit timeline viewer.

## Quick start (backend)

```bash
docker compose up --build
```

API base URL: `http://localhost:8080`

### Example flow

1. Register two users (`POST /api/auth/register`).
2. Creator logs in and creates a capsule for the recipient (`POST /api/capsules`).
3. Add a date condition (`POST /api/capsules/{id}/conditions`).
4. Lock (`POST /api/capsules/{id}/lock`).
5. Wait for scheduler **or** set `unlockAt` in the past for testing.
6. Recipient opens (`POST /api/capsules/{id}/open`) when status is `UNLOCKED`.
7. Inspect audit trail (`GET /api/capsules/{id}/events`).

See `docs/api/rest.md` for endpoints.

## Local development (without Docker for API)

```bash
docker compose up postgres -d
cd backend
./mvnw spring-boot:run
```

## Tests

```bash
cd backend
./mvnw test
```

## Mobile app

```bash
cd mobile
npm start
```

Set `EXPO_PUBLIC_API_URL` if not using defaults (`localhost` on iOS simulator, `10.0.2.2` on Android emulator). Start the API + Postgres first (`docker compose up`).

## Next build steps

1. Client-side encryption (AES-GCM + SecureStore key material).
3. Offline SQLite sync queue + idempotency.
4. Expo push notifications on unlock.

## Documentation

- `docs/architecture/overview.md`
- `docs/database/schema.md`
- `docs/algorithms/evaluation.md`
- `docs/PROPOSAL.md` (product summary)
