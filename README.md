# Zenith Planner - Premium Digital Agenda

Zenith Planner is a multilingual Android planner app built from the provided master prompt. It uses Kotlin, Jetpack Compose, MVVM, Room, Flow, and a premium black, cream, and gold visual language.

## Features

- Dashboard with premium daily focus, task progress, habit progress, goal snapshot, and quick navigation.
- Monthly calendar with date selection and day detail handoff.
- Daily planner with Top 3 tasks, hourly schedule, notes, reflection, and daily habit chips.
- Goal tracking for monthly goals, 90-day plan, and life goals with editable progress sliders.
- Habit tracker with daily completion state and visual completion percentage.
- Motivation page with premium quote presentation and custom quote entry.
- Notes module with persistent note creation and deletion.
- Finance module with income, expense, and balance tracking.
- Daily reminder notifications with boot restore.
- Home screen widget that shows today's top tasks.
- Firebase Authentication + Firestore sync layer with Google Sign-In through Credential Manager.
- Firestore cloud restore from the latest planner snapshot.
- In-app language selection for English, Turkish, Spanish, and French.
- Offline-first local persistence through Room.

## Tech Stack

- Kotlin
- Jetpack Compose and Material 3
- Navigation Compose
- Room database
- Kotlin Coroutines and Flow
- Android notifications and AppWidgetProvider
- Firebase Auth and Firestore
- Android Credential Manager for Google Sign-In
- Manual application-level dependency container

Hilt was intentionally not kept in the final build because the installed AGP 9.1 + kapt toolchain on this machine rejects Hilt processing in this project setup. Room remains kapt-based and the app compiles successfully.

## Build

Open the folder in Android Studio or run:

```powershell
.\gradlew.bat :app:assembleDebug
```

The debug APK is generated at:

```text
app\build\outputs\apk\debug\app-debug.apk
```

## Firebase Setup

Firebase is configured through `app/google-services.json` and the Google Services Gradle plugin. The current file generates `default_web_client_id`, `google_api_key`, `google_app_id`, and `project_id` at build time.

Firebase console requirements:

- Android package name: `Zenith.Planner`
- Add SHA-1/SHA-256 fingerprints for the signing key you use.
- Enable Google provider in Firebase Authentication.
- Enable Cloud Firestore.

The app writes a complete planner snapshot to:

```text
users/{uid}/plannerSnapshots/current
```

The Settings screen can also restore the latest snapshot from the same document back into the local Room database.

Suggested starter Firestore rule:

```text
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId}/{document=**} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

## Language Support

Android resource folders are included for:

- `values` - English default
- `values-tr` - Turkish
- `values-es` - Spanish
- `values-fr` - French

Users can switch language from the Settings screen.

## Notes

The project path contains Turkish characters, so `android.overridePathCheck=true` is set in `gradle.properties` to allow Windows builds in the current folder. The Gradle file also uses classic Kotlin/kapt compatibility flags because Room annotation processing is currently configured through kapt.
