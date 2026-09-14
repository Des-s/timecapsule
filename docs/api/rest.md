# REST API (MVP)

Base path: `/api`

## Auth

| Method | Path | Auth |
|--------|------|------|
| POST | `/auth/register` | no |
| POST | `/auth/login` | no |
| GET | `/auth/me` | JWT |

## Users

| Method | Path | Auth |
|--------|------|------|
| GET | `/users/lookup?email=` | JWT |

JWT header: `Authorization: Bearer <token>`

## Capsules

| Method | Path | Notes |
|--------|------|-------|
| POST | `/capsules` | create draft |
| GET | `/capsules` | creator or recipient |
| GET | `/capsules/{id}` | metadata; content hidden unless unlocked |
| PATCH | `/capsules/{id}` | draft only |
| DELETE | `/capsules/{id}` | draft only |
| POST | `/capsules/{id}/lock` | requires ≥1 condition |
| POST | `/capsules/{id}/open` | recipient; triggers evaluation |

## Conditions

| Method | Path |
|--------|------|
| POST | `/capsules/{id}/conditions` |
| GET | `/capsules/{id}/conditions` |
| DELETE | `/capsules/{id}/conditions/{conditionId}` |
| POST | `/capsules/{id}/location-check` |

### Example date condition body

```json
{
  "type": "DATE",
  "config": {
    "unlockAt": "2026-12-25T00:00:00Z"
  }
}
```

### Example location condition config

```json
{
  "type": "LOCATION",
  "config": {
    "latitude": 6.6745,
    "longitude": -1.5712,
    "radiusMeters": 250
  }
}
```

Identity/connectivity conditions may use `{}` config for MVP markers.

## Audit

| Method | Path |
|--------|------|
| GET | `/capsules/{id}/events` |
