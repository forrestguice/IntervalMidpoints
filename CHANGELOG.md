
### v0.1.1 (2023-02-12)
* adds app launcher icon.
* adds permissions `RECEIVE_BOOT_COMPLETED` and `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` [permission].
* fixes crash when running on Android 11+; adds package visibility queries to the manifest.

### v0.1.0 (2022-02-08)
* UI to find the interval between two twilight events; configuration info (e.g. location, timezone) is supplied by Suntimes.
* UI to find interval midpoints; divide into 2, 3 or 4 parts.
* schedule repeating alarms (or notifications); requires Suntimes V0.14.0.