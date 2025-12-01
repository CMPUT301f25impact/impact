# Firestore Seeding Guide

All seeding assets live under `seeding_assets/`. Follow these steps to reproduce the demo data locally.

## 1. Prerequisites
- Node.js 18+ and npm
- Firebase CLI (`npm install -g firebase-tools`)
- Google Cloud CLI (for `gcloud auth application-default login`) or an ADC-compatible Firebase token
- Access to the same Firebase project referenced by `app/google-services.json`

## 2. Install dependencies
```bash
cd seeding_assets
npm install
```
This installs `firebase-admin` and `firebase-tools` locally so the script in `seeding_assets/script/` can run.

## 3. Authenticate (no service account needed)
```bash
firebase login
# Choose ONE of the following for Application Default Credentials
gcloud auth application-default login
# OR generate a token: firebase login:ci
```
`seed_demo_data.js` uses `admin.credential.applicationDefault()` and automatically loads the project id from `app/google-services.json`, so it always seeds the same Firebase project as the Android app.

## 4. Run the seed
```bash
npm run seed
```
This command executes `node script/seed_demo_data.js`, which:
1. Uploads poster images from `seeding_assets/posters/` into the `images` collection.
2. Ensures demo users (1 organizer, 1 admin, 20 named entrants) exist with deterministic ids.
3. Inserts events `eventA`–`eventE` whose titles/descriptions match the poster themes (hackathon, robotics, science expo, Android bootcamp, study hall). Events A & B end on Dec 1 2025; the rest end on Dec 4–6 2025 as required.
4. Seeds waiting lists with a realistic mix of ~20 entrants spread across `pending`, `selected`, `accepted`, and `cancelled` states—some entrants remain off the waiting list so you can enroll them manually.

## 5. Verifying the data
- Check Firestore console: you should see new docs in `images`, `events`, `users`, and `waitingLists/*/entrants/*`.
- Launch the app and sign in with `organizer@example.com / password123` to exercise organizer flows.

## 6. Rerunning safely
The script uses deterministic document ids and `set(..., { merge: true })`, so it can be run repeatedly without clearing the database. Delete collections manually if you want a clean slate.

## Troubleshooting
- **Missing google-services.json**: ensure `app/google-services.json` exists (it ships with the repo).
- **Permission errors**: re-run `firebase login` and `gcloud auth application-default login`.
- **Poster assets not found**: confirm the PNGs in `seeding_assets/posters/` are still present.

That's it—any teammate can reseed by following this document.
