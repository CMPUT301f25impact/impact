# Firebase Setup Guide

This document explains how to connect the Android app to our shared Firebase project (**Impact Nexus**).

---

##  1. Access the Firebase project
- Go to [Firebase Console](https://console.firebase.google.com/)
- Ask **Aisha** for access to the **Impact Nexus** project.
- Once added as a collaborator, you can download your own configuration file.

---

##  2. Add `google-services.json`
1. In the Firebase Console:
    - Go to **Project settings → Your apps → Android**.
    - Click **Download google-services.json**.
2. Place it in your Android Studio project under: app/google-services.json
3. Make sure the file is **not committed** to Git. It’s already ignored in `.gitignore`.

---

##  3. Firebase dependencies
Firebase has already been configured in `app/build.gradle.kts` with:
kotlin
implementation(platform("com.google.firebase:firebase-bom:34.5.0"))
implementation("com.google.firebase:firebase-analytics")
implementation("com.google.firebase:firebase-auth")
implementation("com.google.firebase:firebase-firestore")

The google-services plugin is also applied in the same file.

## 🔹4. Gradle plugin versions

Our Gradle configuration uses:

id("com.android.application") version "8.6.1" apply false
id("org.jetbrains.kotlin.android") version "1.9.25" apply false
id("com.google.gms.google-services") version "4.4.2" apply false


Firebase SDK works with:

compileSdk = 36

minSdk = 24

targetSdk = 36

🔹 5. Verify your setup

After adding your google-services.json, sync Gradle:

In Android Studio: File → Sync Project with Gradle Files

Run the app once

Check Logcat for:

FirebaseApp initialization successful


That means you’re connected!

🔹 6. Troubleshooting

If Gradle fails to sync:

Make sure you placed google-services.json in the app/ folder.

Verify that you didn’t rename it.

Rebuild the project (Build → Clean Project → Rebuild Project).

If issues persist, contact Aisha for the latest project configuration.

