# TimeCapsule mobile (planned)

Expo + React Native + TypeScript, Expo Router, Zustand, React Hook Form + Zod.

## MVP screens

- Auth (register/login)
- Capsule list (sent/received, status badges)
- Create/edit draft capsule
- Condition builder (date picker, map pin + radius, recipient)
- Lock confirmation
- Capsule detail (locked vs unlocked)
- Foreground location check → `POST /api/capsules/{id}/location-check`
- Audit timeline

## Local data (phase 7)

Expo SQLite: `local_capsules`, `pending_operations`, sync on reconnect.

Scaffold with:

```bash
npx create-expo-app@latest . --template tabs
```

(Not generated yet — backend milestone first.)
