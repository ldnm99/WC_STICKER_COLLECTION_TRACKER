# ⚽ WC 2026 Sticker Tracker

A native Android app to track your **FIFA World Cup 2026 Panini sticker collection** — built for real trading sessions.

Know exactly what you're missing, what you have doubles of, and quickly log new stickers as you open packets.

---

## Features

- 📋 **Full sticker checklist** — all 980 stickers across 48 teams + FWC special section
- ✅ **Mark owned stickers** — tap any sticker card to set your quantity
- 🔁 **Duplicate tracking** — know exactly how many extras you have per sticker (for trading)
- ❌ **Missing stickers list** — see at a glance what you still need
- ⚡ **Quick Add** — type sticker IDs like `ARG1 ENG5 FWC9` to log a whole packet at once, then review a lightweight session summary
- 🌍 **Browse by team** — teams grouped by confederation with per-team progress
- 📊 **Collection progress** — live stats on home screen (collected / missing / duplicates)
- 📈 **KPI insights** — see sticker counts per section, most completed team, and duplicate leaders
- 💾 **100% offline** — all data stored locally, nothing sent to the internet

---

## Sticker Breakdown

| Confederation | Teams | Stickers |
|---|---|---|
| ⭐ Special (FWC) | — | 20 |
| 🌎 CONCACAF | Mexico, USA, Canada *(hosts)*, Panama, Curaçao, Haiti | 120 |
| 🌍 UEFA | England, France, Spain, Germany, Netherlands, Portugal, Belgium, Croatia, Switzerland, Austria, Norway, Scotland, Sweden, Türkiye, Bosnia & Herzegovina, Czechia | 320 |
| 🌎 CONMEBOL | Argentina, Brazil, Uruguay, Colombia, Ecuador, Paraguay | 120 |
| 🌍 CAF | Morocco, Senegal, Egypt, Tunisia, Ghana, Côte d'Ivoire, Algeria, Cape Verde, South Africa, DR Congo | 200 |
| 🌏 AFC | Japan, South Korea, Iran, Australia, Saudi Arabia, Qatar, Uzbekistan, Iraq, Jordan | 180 |
| 🌏 OFC | New Zealand | 20 |
| **Total** | **48 teams** | **980 stickers** |

Each team has **20 stickers**: Badge (foil) · Team Photo · 18 Players

---

## Requirements

| Requirement | Version |
|---|---|
| Android | **8.0 (API 26)** or higher |
| Android Studio | **Hedgehog (2023.1.1)** or higher |
| JDK | **17** |
| Kotlin | 2.0.0 |
| Gradle | 8.5+ |

---

## Installation

### Option A — Build from Source (Android Studio)

1. **Clone the repository**
   ```bash
   git clone https://github.com/ldnm99/WC_STICKER_COLLECTION_TRACKER.git
   cd WC_STICKER_COLLECTION_TRACKER
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select **File → Open** and navigate to the cloned folder
   - Wait for Gradle sync to complete (first time may take 2–5 minutes while downloading dependencies)

3. **Connect your device or start an emulator**
   - **Physical device:** Enable *USB debugging* in *Developer Options* and connect via USB
   - **Emulator:** Go to *Device Manager → Create Device* (recommend Pixel 6, API 33+)

4. **Run the app**
    - Click the green ▶ **Run** button (or press `Shift+F10`)
    - Select your device
    - The app will build and launch automatically
    - Or build from the terminal on Windows with `.\gradlew.bat assembleDebug`

> On first launch the app seeds the full sticker database. This takes ~1–2 seconds and happens only once.

---

### Option B — Install APK (Sideload)

If you have a pre-built `.apk` file:

1. **Transfer the APK** to your Android device (via USB, email, Google Drive, etc.)

2. **Allow installs from unknown sources**
   - Go to *Settings → Security* (or *Apps → Special app access → Install unknown apps*)
   - Enable for the app you're using to open the file (e.g. Files, Chrome)

3. **Open the APK file** on your device and tap **Install**

4. **Launch** from your app drawer

---

## How to Use

### Logging stickers from a new packet

1. Tap the **➕ Quick Add** button (floating button on Home or Teams screen)
2. Type your sticker IDs — format is `TEAMCODE + NUMBER` with no separator:
   - Examples: `ARG1`, `ENG15`, `FWC9`, `MAR7`
3. Valid IDs appear as **green chips**, unrecognized ones as **red chips**
4. Tap **Add to Collection** — each sticker's count increases by 1
5. Review the session summary to see what was new, what turned into duplicates, and which teams moved closer to completion

### Checking your duplicates (for trading)

- From the Home screen, tap **📋 Duplicates**
- You'll see every sticker you own 2+ copies of, with the count of extras
- Use this list when arranging trades with friends

### Checking what you're missing

- From the Home screen, tap **❌ Missing Stickers**
- Full list of stickers you haven't collected yet, grouped by team

### Browsing by team

- Tap **🏆 Browse by Team** on the Home screen
- Teams are grouped by confederation
- Each row shows a progress bar (e.g. 14/20 collected)
- Tap a team to open its sticker grid
- Tap any sticker card to open the quantity picker

---

## Screenshots

> Add screenshots here — run the app on a Pixel 6 emulator (API 33+), take screenshots,  
> save them to `screenshots/` and embed them below.

| Home | Teams | Quick Add |
|------|-------|-----------|
| *(coming soon)* | *(coming soon)* | *(coming soon)* |

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material Design 3 |
| Architecture | MVVM + Repository |
| Database | Room (SQLite) |
| State | StateFlow + ViewModel |
| Navigation | Navigation Compose |
| DI | Hilt |

---

## Project Structure

```
app/src/main/java/com/wc2026stickers/app/
├── data/
│   ├── db/
│   │   ├── AppDatabase.kt          # Room database
│   │   ├── Converters.kt           # TypeConverters
│   │   ├── entities/               # Team, Sticker, UserSticker
│   │   └── dao/                    # TeamDao, StickerDao
│   ├── repository/
│   │   └── StickerRepository.kt    # Single source of truth
│   └── seed/
│       └── DatabaseSeeder.kt       # First-launch data seeding
├── di/
│   └── DatabaseModule.kt           # Hilt DI wiring
├── navigation/
│   └── AppNavigation.kt            # NavHost + routes
└── ui/
    ├── home/                        # Home dashboard
    ├── teams/                       # Teams list
    ├── teamdetail/                  # Sticker grid per team
    ├── missing/                     # Missing stickers
    ├── duplicates/                  # Duplicate stickers
    ├── quickadd/                    # Quick Add by ID
    ├── components/                  # Shared composables
    └── theme/                       # Colours & typography
```

---

## License

MIT — see [LICENSE](LICENSE) for details.
