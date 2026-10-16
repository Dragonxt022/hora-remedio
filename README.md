<img src="docs/banner.svg" alt="Hora do Remédio" width="100%"/>

# Hora do Remédio

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Room](https://img.shields.io/badge/Room-SQLite-3DDC84?logo=android&logoColor=white)](https://developer.android.com/training/data-storage/room)
[![minSdk](https://img.shields.io/badge/minSdk-26-3DDC84?logo=android&logoColor=white)](https://developer.android.com)
[![Version](https://img.shields.io/badge/version-1.1-00A878)](#)

**English** · [Português :brazil:](README.pt-BR.md)

A modern Android app for **medication reminders**, organizing by **profiles**
(you, mom, dad, pet…) and tracking health with a **medical record**, a
**symptom diary** and **adherence statistics**. Offline-first and privacy-first:
**all data stays on your device**.

---

## 📱 Screens

<table>
  <tr>
    <td align="center"><img src="docs/screenshots/01-doses.png" width="220"/><br/><sub><b>Today's doses</b></sub></td>
    <td align="center"><img src="docs/screenshots/02-remedios.png" width="220"/><br/><sub><b>My medications</b></sub></td>
    <td align="center"><img src="docs/screenshots/04-saude.png" width="220"/><br/><sub><b>Health (record + charts)</b></sub></td>
  </tr>
  <tr>
    <td align="center"><img src="docs/screenshots/05-estatisticas.png" width="220"/><br/><sub><b>Statistics &amp; adherence</b></sub></td>
    <td align="center"><img src="docs/screenshots/07-detalhe.png" width="220"/><br/><sub><b>Medication detail</b></sub></td>
    <td align="center"><img src="docs/screenshots/06-perfil.png" width="220"/><br/><sub><b>Profile &amp; settings</b></sub></td>
  </tr>
</table>

---

## ✨ Features

### ⏰ Reliable reminders
- **Exact alarms** (`setAlarmClock` + `AlarmManager`) that survive Doze mode.
- **Automatic rescheduling** after reboot, time-zone/time changes or app updates (`WorkManager` + `BroadcastReceiver`).
- Notifications with **Take now** and **Snooze** actions (configurable: 5/10/15/30 min), with **per-medication sound/vibration**.
- Guided permission flow (notifications, exact alarms and battery optimization).

### 💊 Medications & profiles
- Add medications with a **catalog/autocomplete**, dosage, times, stock and a **run-out forecast** ("runs out in X days", "refill suggested").
- **Customizable profiles** (add, rename, remove) — renaming **cascades** to medications, medical record and diary.
- **Multiple profiles** (e.g., Me, Mom, Dad, Son, Pet) with a per-profile filter.

### 🩺 Health
- **Per-profile medical record**: blood type, allergies, chronic conditions, weight, height, emergency contact and notes.
- **Health diary** with a **unified timeline** that automatically merges **medication intakes** + logged events (symptoms, fever, pressure, notes).
- **Photo attachments** on entries and a **temperature chart**.
- **Allergy warning** when registering a medication that matches the profile's allergy.

### 📊 Statistics & reports
- Adherence with a **period filter** (7/30/90 days or all), **by profile** and **by medication**.
- **Streak** of fully-adherent days.
- Export a **CSV** and a **PDF** report (including the medical record).

### 🔒 Privacy & extras
- **App lock** with biometrics/PIN.
- **Backup & restore** (JSON) and **delete all data**.
- Built-in **privacy policy**.
- Next-dose **widget** and a **subscription** base (Google Play Billing).
- **Light/dark theme** and **English/Portuguese** support.

---

## 🛠️ Tech stack

| Layer | Stack |
|-------|-------|
| Language | **Kotlin** |
| UI | **Jetpack Compose** + Material 3 |
| Navigation | Navigation Compose |
| Persistence | **Room** (versioned migrations + tests) |
| Async | Coroutines + Flow |
| Background work | **WorkManager** + AlarmManager |
| Preferences | DataStore |
| Images | Coil |
| Security | AndroidX Biometric |
| Widgets | Glance |
| Monetization | Google Play Billing |
| Build | Gradle (Kotlin DSL) + version Catalog |

**minSdk 26 · targetSdk 37**

---

## 🏛️ Architecture

Layered architecture (MVVM + Repository), built for testability:

```
app/
├─ data/         Room entities, DAOs, repositories (Backup, Billing, Settings, Profile…)
├─ domain/       Pure, testable business rules
│                (DoseAdherence, AdherenceStreak, InventoryForecast,
│                 AllergyMatcher, HealthMetrics, ReportCsv, PremiumLimits…)
├─ notifications/ Alarms, notification channels, workers and receivers
├─ ui/           Compose screens, ViewModel (single state), dialogs and themes
└─ widget/       Next-dose widget (Glance)
```

- **Single state** exposed by `MedicationViewModel` (combining multiple `Flow`s).
- **Tested Room migrations** (v3→v8) with `MigrationTestHelper`.
- **Centralized design tokens** (surface colors, borders, radii).

---

## 🚀 Build

Requirements: **JDK 17+** and the **Android SDK**.

```bash
# Debug build
./gradlew assembleDebug

# Install on a connected device
./gradlew installDebug

# Unit tests + lint
./gradlew testDebugUnitTest lintDebug

# Instrumented tests (migrations) — needs a device/emulator
./gradlew connectedDebugAndroidTest
```

### Signed release
Create a `keystore.properties` at the root (not versioned):

```properties
storeFile=keystore/aprobile-release.jks
storePassword=****
keyAlias=aprobile
keyPassword=****
```

```bash
./gradlew assembleRelease   # app/build/outputs/apk/release/app-release.apk
```

---

## 🧪 Simulation tool (debug)

In **debug** builds, the Profile screen includes **"Simulate 1 year of data"**,
which generates ~1 year of dose history and fever episodes to test period
filters, charts and statistics. It is not shown in release builds.

---

## 🔐 Privacy

Hora do Remédio is **offline-first**: no data is sent to servers. Everything is
stored locally (Room + DataStore). You can **export**, **import** or **delete**
your data at any time from the *Privacy & data* screen.

---

## 📄 License

© 2026 — Hora do Remédio. All rights reserved.
