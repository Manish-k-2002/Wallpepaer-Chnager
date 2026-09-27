# Wallpaper Changer

A minimal, non-vibe-coded Android app that automatically changes your phone's
wallpaper on a daily schedule, using a random photo from
[Picsum](https://picsum.photos) (no API key needed).

## How it works

| Piece | File | Purpose |
|---|---|---|
| UI | `MainActivity.kt` + `activity_main.xml` | "Change now" button, daily on/off switch |
| Network | `ImageRepository.kt` | Downloads a random photo sized to your screen |
| Wallpaper API | `WallpaperWorker.kt` | Applies the photo via Android's `WallpaperManager` |
| Scheduling | `WallpaperWorker.kt` (via `WorkManager`) | Runs reliably every 24h, even if the app is closed |

`WorkManager` is used instead of a `Timer`/`Handler` because Android kills
background threads when an app isn't open — `WorkManager` persists the job
and Android itself wakes the app briefly to run it.

## Run it locally

1. Install **Android Studio** (free): https://developer.android.com/studio
2. Open this folder as a project (`File > Open`)
3. Let Gradle sync (first time takes a few minutes — it downloads dependencies)
4. Connect a phone via USB (with Developer Options + USB debugging on) or start an emulator
5. Click the green ▶ Run button

## Build a standalone APK

In Android Studio: `Build > Build Bundle(s) / APK(s) > Build APK(s)`.
The APK lands in `app/build/outputs/apk/debug/app-debug.apk` — copy that to
your phone and install it (you'll need to allow "install from unknown
sources" once).

## Push this to your own GitHub repo

```bash
cd WallpaperChanger
git init
git add .
git commit -m "Initial commit: wallpaper changer app"
git branch -M main
git remote add origin https://github.com/<your-username>/<your-repo-name>.git
git push -u origin main
```

(Create the empty repo on GitHub first at github.com/new — don't initialize
it with a README there, since this project already has one.)

## Next steps you might want

- Let a user pick change frequency (every X hours) instead of a fixed 24h
- Add a category filter (Picsum supports grayscale/blur query params)
- Switch to Unsplash's API for curated, higher-quality photos (needs a free key)
- Add a "Run on device boot" `BroadcastReceiver` so the schedule re-registers after a restart (WorkManager usually survives reboots on its own, but this adds a safety net)
