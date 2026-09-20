# Contributing to CCTV Viewer

Bug reports, fixes, documentation improvements, and translations are welcome. Please follow the [Code of Conduct](CODE_OF_CONDUCT.md) in project discussions.

## Before You Start

Search existing issues and pull requests before opening a new one. For substantial features or architectural changes, open an issue to discuss the scope before implementation. Small fixes can go directly to a pull request.

CCTV Viewer displays camera web pages on Android TV. It accepts HTTP and HTTPS URLs and does not include a native RTSP player. Describe requests in terms of the viewing or setup problem you want to solve.

## Development Setup

Use JDK 21, Android SDK 37, and an Android Studio version compatible with the Android Gradle Plugin version in [gradle/libs.versions.toml](gradle/libs.versions.toml). Use the included Gradle wrapper. Android TV devices and emulators must run Android 8.0 (API 26) or later.

1. Fork and clone the repository, then create a branch for your change.
2. Open the repository in Android Studio and sync Gradle.
3. Build the app without embedding local camera settings:

   ```sh
   ./gradlew :app:assembleDebug -PincludeLocalCameras=false
   ```

On Windows, use `gradlew.bat` instead of `./gradlew`.

Camera settings are optional for building and running unit tests. For manual testing, configure a camera through the app's Settings screen, or copy [cameras.example.properties](cameras.example.properties) to `cameras.local.properties` and fill in your own values. To use those values, omit `-PincludeLocalCameras=false` when building.

Local camera settings are embedded in the APK by default. Use `-PincludeLocalCameras=false` for any build you intend to share. Do not commit camera credentials, private URLs, signing keys, or local configuration files. Remove private information from logs, screenshots, and recordings, including setup QR codes.

## Making Changes

Follow the existing Kotlin, Compose, and dependency patterns. Read the [software architecture](docs/software-architecture.md) before changing startup, camera viewing, scheduling, or component responsibilities; this document is currently in Traditional Chinese.

Keep each pull request focused on one change. Add or update tests when behavior changes, and update the relevant documentation. Discuss new dependencies or architectural patterns before introducing them.

### Translations

TV controls and the phone setup page share Android string resources in `app/src/main/res/values*/strings.xml`. Keep resource keys and format placeholders consistent across supported languages. The default `values` resources and `values-zh-rTW` must remain identical.

When adding a language, update the locale declarations, language mapping, and translation tests as well as the string resources. Check the TV interface and phone setup page for truncation and formatting issues.

## Validation

For code changes, run:

```sh
./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug -PincludeLocalCameras=false
```

Unit tests cover settings storage, the local configuration server, language handling, and ViewModel behavior. They do not require a real camera. WebView playback and remote-control navigation also need manual verification on an Android TV emulator or device when affected by a change.

For UI or playback changes, check focus navigation, camera switching, page refresh, and background/foreground behavior as applicable. For phone setup changes, check opening the setup link, saving settings, and closing the settings dialog. Include the device or emulator, Android version, and WebView version in your results.

Documentation-only changes need a check of links and formatting; an app build is not required. State which checks you completed and explain any relevant checks you could not run.

## Pull Requests

Use the pull request template to explain the problem, resulting behavior, and validation. Link related issues and include screenshots for visible changes after removing private information.

Keep commit messages and PR titles concise and professional, without emojis. Conventional Commit titles such as `fix(settings): preserve camera names` are recommended. AI-assisted commits must include an appropriate `Co-Authored-By` trailer identifying the assistant used.

Release notes are maintained in GitHub Releases; no separate changelog file is required.

## Licensing

By submitting a contribution, you agree to license your original contributions under the project's [MIT License](LICENSE). Identify the source and applicable license of any third-party code or assets you include, and retain required notices. See [Third-Party Notices](THIRD_PARTY_NOTICES.md) for the current attribution record.
