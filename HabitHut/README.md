# Habit Hut (Android, Kotlin)

Features:
- App- and goal-tracking with local Room database; streaks via completions per day
- Reminders via WorkManager notifications
- Focus mode overlay service to block distractions (requires Usage Access and Draw Over Other Apps)
- Free tier: 1 blocked app and up to 3 active habits/goals
- Premium: unlimited via Google Play Billing ($5/month `habit_hut_monthly`, $50/year `habit_hut_yearly`)

## Build instructions
1. Open this project in Android Studio (Giraffe+ recommended).
2. Ensure JDK 17 is configured in Gradle settings.
3. Sync Gradle.
4. Create Play Billing products in the Play Console with IDs:
   - `habit_hut_monthly` (SUBS)
   - `habit_hut_yearly` (SUBS)
5. Run on a device (Android 8.0+ recommended). For focus overlay:
   - Grant Draw over other apps: Settings > Apps > Special app access > Display over other apps
   - Grant Usage Access (optional for advanced blocking): Settings > Apps > Special app access > Usage access

## Notes
- This sample includes a basic overlay-based blocker; full app-switch detection via `UsageStatsManager` can be added as needed.
- All data is stored locally in Room; export/sync not included.