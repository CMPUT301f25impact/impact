#!/usr/bin/env node

/**
 * Firestore seeding script for CMPUT301 Impact.
 *
 * Authentication requirements:
 *   1. `firebase login`
 *   2. `gcloud auth application-default login` (or provide `GOOGLE_APPLICATION_CREDENTIALS` via Firebase CLI token)
 *
 * The script never uses a service-account key. It reads the project id from
 * `app/google-services.json` so it always targets the same Firebase project as
 * the Android app.
 */

const admin = require("firebase-admin");
const fs = require("fs");
const path = require("path");

// ---------------------------------------------------------
// 1. Resolve project id from the Android google-services.json
// ---------------------------------------------------------
const ROOT_DIR = path.join(__dirname, "..", "..");
const googleServicesPath = path.join(ROOT_DIR, "app", "google-services.json");

if (!fs.existsSync(googleServicesPath)) {
  throw new Error(
    `Unable to locate google-services.json at ${googleServicesPath}. Make sure the Android config exists.`
  );
}

const googleServices = JSON.parse(fs.readFileSync(googleServicesPath, "utf-8"));
const projectId = googleServices?.project_info?.project_id;

if (!projectId) {
  throw new Error("google-services.json is missing project_info.project_id");
}

console.log("🔥 Using Firebase project:", projectId);

// ---------------------------------------------------------
// 2. Initialize Firebase Admin via Application Default Credentials
// ---------------------------------------------------------
admin.initializeApp({
  credential: admin.credential.applicationDefault(),
  projectId,
});

const db = admin.firestore();
const FieldValue = admin.firestore.FieldValue;

// ---------------------------------------------------------
// Helpers
// ---------------------------------------------------------
const POSTER_DIR = path.join(__dirname, "..", "posters");

function utcDate(year, month, day, hour = 0, minute = 0) {
  return new Date(Date.UTC(year, month - 1, day, hour, minute, 0));
}

function ensurePosterExists(fileName) {
  const fullPath = path.join(POSTER_DIR, fileName);
  if (!fs.existsSync(fullPath)) {
    throw new Error(`Poster asset missing: ${fullPath}`);
  }
  return fullPath;
}

async function createImageDoc(fileName) {
  const posterPath = ensurePosterExists(fileName);
  const base64 = fs.readFileSync(posterPath).toString("base64");
  const docRef = db.collection("images").doc();
  await docRef.set({
    base64Content: base64,
    mimeType: "image/png",
    fileName,
  });
  return docRef.id;
}

async function upsertUserDoc(id, profile) {
  await db
    .collection("users")
    .doc(id)
    .set(
      {
        id,
        email: profile.email,
        password: profile.password,
        role: profile.role,
        name: profile.name || null,
        phone: profile.phone || null,
        deviceId: `device-${id}`,
        notificationsEnabled: true,
        createdAt: FieldValue.serverTimestamp(),
      },
      { merge: true }
    );
}

async function seedWaitingList(eventId, eventName, entrantId, status) {
  await db
    .collection("waitingLists")
    .doc(eventId)
    .collection("entrants")
    .doc(entrantId)
    .set({
      eventId,
      eventName,
      entrantId,
      status,
      timestamp: FieldValue.serverTimestamp(),
    });
}

// ---------------------------------------------------------
// Main seeding flow
// ---------------------------------------------------------
async function main() {
  console.log("🌱 Starting seed…");

  // ---------------------
  // USERS
  // ---------------------
  const organizerId = "organizer-1";
  const entrants = [
    { id: "entrant-01", name: "Alex Rivers" },
    { id: "entrant-02", name: "Bailey Chen" },
    { id: "entrant-03", name: "Casey Patel" },
    { id: "entrant-04", name: "Devon Singh" },
    { id: "entrant-05", name: "Emery Scott" },
    { id: "entrant-06", name: "Finley Brooks" },
    { id: "entrant-07", name: "Gabe Morales" },
    { id: "entrant-08", name: "Hayden Clarke" },
    { id: "entrant-09", name: "Indie Watts" },
    { id: "entrant-10", name: "Jules Romero" },
    { id: "entrant-11", name: "Kendall Wu" },
    { id: "entrant-12", name: "Logan Price" },
    { id: "entrant-13", name: "Morgan Blake" },
    { id: "entrant-14", name: "Noel Ford" },
    { id: "entrant-15", name: "Oakley Reese" },
    { id: "entrant-16", name: "Parker Hale" },
    { id: "entrant-17", name: "Quinn Vega" },
    { id: "entrant-18", name: "Reese Abbott" },
    { id: "entrant-19", name: "Sidney Lowe" },
    { id: "entrant-20", name: "Taylor Knox" },
  ];

  const userPromises = entrants.map((entrant) =>
    upsertUserDoc(entrant.id, {
      email: `${entrant.id}@example.com`,
      password: "password123",
      role: "entrant",
      name: entrant.name,
    })
  );

  userPromises.push(
    upsertUserDoc(organizerId, {
      email: "organizer@example.com",
      password: "password123",
      role: "organizer",
      name: "Demo Organizer",
    })
  );
  userPromises.push(
    upsertUserDoc("admin-1", {
      email: "admin@example.com",
      password: "password123",
      role: "admin",
      name: "Demo Admin",
    })
  );

  await Promise.all(userPromises);

  console.log("✅ Users seeded.");

  // ---------------------
  // IMAGES
  // ---------------------
  console.log("🖼  Creating poster image docs…");
  const posterIds = {
    A: await createImageDoc("eventA_hackathon.png"),
    B: await createImageDoc("eventB_robotics.png"),
    C: await createImageDoc("eventC_sciencefair.png"),
    D: await createImageDoc("eventD_bootcamp.png"),
    E: await createImageDoc("eventE_studyhall.png"),
  };
  console.log("✅ Posters uploaded:", posterIds);

  // ---------------------
  // EVENTS
  // ---------------------
  const events = [
    {
      id: "eventA",
      name: "Campus Hackathon Kickoff",
      description: "48-hour hackathon filled with mentorship, prizes, and midnight pizza.",
      startDate: utcDate(2025, 11, 25, 9, 0),
      endDate: utcDate(2025, 12, 1, 18, 0),
      posterId: posterIds.A,
      tags: ["hackathon", "coding"],
      capacity: 6,
      lotteryDone: false,
    },
    {
      id: "eventB",
      name: "Robotics Show & Build",
      description: "Hands-on robotics lab showing off autonomous builds from local teams.",
      startDate: utcDate(2025, 11, 26, 10, 0),
      endDate: utcDate(2025, 12, 1, 20, 0),
      posterId: posterIds.B,
      tags: ["robotics", "engineering"],
      capacity: 5,
      lotteryDone: true,
    },
    {
      id: "eventC",
      name: "Community Science Expo",
      description: "Interactive science fair booths with live experiments for all ages.",
      startDate: utcDate(2025, 11, 27, 11, 0),
      endDate: utcDate(2025, 12, 4, 21, 0),
      posterId: posterIds.C,
      tags: ["science", "community"],
      capacity: 8,
      lotteryDone: true,
    },
    {
      id: "eventD",
      name: "Android Coding Bootcamp",
      description: "Weeklong immersive Android workshop to ship your first Kotlin app.",
      startDate: utcDate(2025, 11, 28, 12, 0),
      endDate: utcDate(2025, 12, 5, 22, 0),
      posterId: posterIds.D,
      tags: ["android", "bootcamp"],
      capacity: 8,
      lotteryDone: true,
    },
    {
      id: "eventE",
      name: "Finals Quiet Study Hall",
      description: "Silent study hall with free snacks, focus playlists, and late hours.",
      startDate: utcDate(2025, 11, 29, 13, 0),
      endDate: utcDate(2025, 12, 6, 23, 0),
      posterId: posterIds.E,
      tags: ["study", "wellness"],
      capacity: 20,
      lotteryDone: false,
    },
  ];

  console.log("🗂  Uploading events…");
  for (const event of events) {
    await db
      .collection("events")
      .doc(event.id)
      .set(
        {
          name: event.name,
          description: event.description,
          startDate: event.startDate,
          endDate: event.endDate,
          organizerId,
          organizerEmail: "organizer@example.com", // legacy compatibility
          posterUrl: event.posterId,
          tags: event.tags,
          capacity: event.capacity,
          lottery_done: event.lotteryDone,
          qrPayload: `impact://event/${event.id}`,
        },
        { merge: true }
      );
  }
  console.log("✅ Events seeded.");

  // ---------------------
  // WAITING LISTS
  // ---------------------
  console.log("👥 Seeding waiting lists…");

  const waitingListSeeds = [
    {
      eventId: "eventA",
      name: events[0].name,
      entries: [
        { entrantId: "entrant-01", status: "pending" },
        { entrantId: "entrant-02", status: "pending" },
        { entrantId: "entrant-03", status: "pending" },
        { entrantId: "entrant-04", status: "pending" },
        { entrantId: "entrant-05", status: "pending" },
      ],
    },
    {
      eventId: "eventB",
      name: events[1].name,
      entries: [
        { entrantId: "entrant-06", status: "selected" },
        { entrantId: "entrant-07", status: "selected" },
        { entrantId: "entrant-08", status: "pending" },
        { entrantId: "entrant-09", status: "pending" },
        { entrantId: "entrant-10", status: "pending" },
      ],
    },
    {
      eventId: "eventC",
      name: events[2].name,
      entries: [
        { entrantId: "entrant-11", status: "accepted" },
        { entrantId: "entrant-12", status: "accepted" },
        { entrantId: "entrant-13", status: "selected" },
        { entrantId: "entrant-14", status: "cancelled" },
        { entrantId: "entrant-15", status: "pending" },
      ],
    },
    {
      eventId: "eventD",
      name: events[3].name,
      entries: [
        { entrantId: "entrant-16", status: "accepted" },
        { entrantId: "entrant-17", status: "accepted" },
        { entrantId: "entrant-18", status: "selected" },
        { entrantId: "entrant-19", status: "pending" },
        { entrantId: "entrant-20", status: "pending" },
      ],
    },
    {
      eventId: "eventE",
      name: events[4].name,
      entries: [],
    },
  ];

  for (const bucket of waitingListSeeds) {
    await Promise.all(
      bucket.entries.map((entry) =>
        seedWaitingList(bucket.eventId, bucket.name, entry.entrantId, entry.status)
      )
    );
  }

  console.log("🎉 Waiting list snapshots seeded (some entrants intentionally unused).");
  console.log("🌱 Seeding complete.");
}

main()
  .then(() => process.exit(0))
  .catch((err) => {
    console.error("❌ Seed error:", err);
    process.exit(1);
  });
