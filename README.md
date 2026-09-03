# deChart

DeChart is an Android application for reading and displaying various types of spectral data.
Reads FITS, .100 and .fds files used by [DECH Software](http://www.gazinur.com/Download.html)

The project has been migrated from the legacy Eclipse/Ant stack to **Android Studio Panda 3** and **Gradle 8.14**.

## Dependencies
* [AChartEngine](https://github.com/ddanny/achartengine) — An open-source charting library for Android applications (pre-built JAR v1.2.0 integrated into `app/libs/`).

## Features & Architecture
* Reads binary data.
* Renders interactive XY line charts using the **AChartEngine** library.
* **Streamlined Debugging**: Test data files (`data.100` and `waves.fds`) are isolated inside `src/debug/assets`. They are automatically extracted into the device's internal cache upon debug app launch. These binary files are excluded from release builds.
* **Tech Stack**: Java, AndroidX `AppCompatActivity` with dynamic themes, Gradle 8.14 (Kotlin DSL), and automatic JDK provisioning via Foojay Toolchains.

## Quick Start & ADB Automation

To clean, build, and force-reinstall the debug (`testOnly`) version directly onto your connected smartphone, run the following commands in your terminal (PowerShell / Bash):

```powershell
# 1. Clean and rebuild the project
./gradlew clean build

# 2. Reinstall with replacement (-r) and test flag (-t) enabled
adb install -r -t app/build/outputs/apk/debug/app-debug.apk
```

## History
* [First probe with Eclipse + Android SDK and AChartEngine](https://juick.com/gvard/2397281), 2013-06-10
* [deChart reads FITS](https://juick.com/gvard/2402630), 2013-06-14
* [Message describing the latest version](https://juick.com/gvard/2407712), 2013-06-18
