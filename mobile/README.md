# TimeCapsule mobile

Expo Router app for the TimeCapsule MVP.

## Run

```bash
npm install
npm start
```

Backend must be running. Configure API URL if needed:

```bash
# Windows PowerShell — physical device on same Wi‑Fi
$env:EXPO_PUBLIC_API_URL="http://192.168.x.x:8080"
npm start
```

Defaults: iOS simulator → `http://localhost:8080`, Android emulator → `http://10.0.2.2:8080`.

## Flows implemented

- Register / login (JWT in SecureStore)
- List capsules
- Create draft (self or lookup recipient by email)
- Add date, identity, location conditions
- Lock capsule
- Recipient: try open + foreground location check
- Audit timeline on capsule detail

## Stack

Expo SDK 57, TypeScript, Zustand, expo-secure-store, expo-location, datetime picker.
