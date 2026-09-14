# TimeCapsule — product summary

**Create now. Lock it. Let the future decide when it can be opened.**

TimeCapsule is a secure mobile application for **conditional digital access**: encrypted capsules unlock only when configured rules pass (date/time, location, recipient identity, and later connectivity/events).

## Portfolio positioning

- Demonstrates a real **condition engine** (not scheduled messaging only).
- Backend-authoritative unlock with **audit timeline** for demos.
- Clear path to events/B2B (scavenger hunts, campus experiences).

## Official MVP (v1.0)

Authenticated users create encrypted capsules for themselves or others with **date**, **identity**, and **location** conditions. The backend evaluates rules, transitions capsule state, notifies recipients (mobile phase), and records immutable events.

## Build order (recommended)

1. Backend prototype: user → capsule → date condition → lock → scheduler → unlock → audit (**implemented first**).
2. Expo mobile client.
3. Encryption, offline sync, push notifications, polish.

Full narrative spec: see project chat / export; technical docs live under `docs/`.
