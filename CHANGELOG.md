
### v0.1.2 (2024-11-18)
* adds support for system theme (night mode), high contrast themes, and "text size" settings.
* fixes bug where app icon is not displayed (#5).
* fixes bug where battery optimization message is displayed repeatedly (#6).
* fixes crash when Suntimes is not installed (#7).
* fixes ANR when Suntimes ContentProvider fails to respond.
* updates build; targetSdkVersion 30 -> 33; Gradle 5.6.4 -> 6.5; Android Gradle Plugin 3.6.1 -> 4.1.3; migrates from legacy support libraries to AndroidX.
* updates SuntimesAddon dependency (v0.4.0).

### v0.1.1 (2023-02-12)
* adds app launcher icon.
* adds permissions `RECEIVE_BOOT_COMPLETED` and `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` [permission].
* fixes crash when running on Android 11+; adds package visibility queries to the manifest.

### v0.1.0 (2022-02-08)
* UI to find the interval between two twilight events; configuration info (e.g. location, timezone) is supplied by Suntimes.
* UI to find interval midpoints; divide into 2, 3 or 4 parts.
* schedule repeating alarms (or notifications); requires Suntimes V0.14.0.