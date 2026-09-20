# Third-Party Notices

Third-party materials retain their own licenses and copyright notices. The project's MIT License does not replace the terms for those materials.

## App Icon

The app icon is based on the [security camera icon on Flaticon](https://www.flaticon.com/free-icon/security-camera_7144872), as credited in the project README. Related files include `assets/security-camera.png`, `app/src/main/ic_launcher-playstore.png`, and launcher images in `app/src/main/res/mipmap-*`.

**Pending maintainer review:** Confirm the icon creator, the license obtained when the asset was downloaded, the required attribution wording, and permission to distribute the image files in a public source repository. The repository does not currently include that license certificate. This entry records the source; it does not establish redistribution permission. See the [Flaticon terms](https://www.flaticon.com/legal).

## TV Banner Font

The TV banner uses jf open Huninn by justfont. The repository contains the rendered banner at `app/src/main/res/drawable/app_icon_your_company.png`, rather than a bundled font file.

- Project: [jf open Huninn](https://github.com/justfont/open-huninn-font)
- Font license: [SIL Open Font License 1.1 and upstream copyright notices](https://github.com/justfont/open-huninn-font/blob/master/LICENSE)

## Software Dependencies

Direct dependencies and their versions are declared in [gradle/libs.versions.toml](gradle/libs.versions.toml) and used in [app/build.gradle.kts](app/build.gradle.kts). This source attribution record is not a complete inventory of transitive dependencies or a substitute for the notices required with a distributed APK.

| Component | Purpose | Upstream license |
| --- | --- | --- |
| AndroidX Compose, Activity, TV Material, Lifecycle, and DataStore | UI, lifecycle, and settings storage | [Apache License 2.0](https://github.com/androidx/androidx/blob/androidx-main/LICENSE.txt) |
| kotlinx.coroutines | Asynchronous operations | [Apache License 2.0](https://github.com/Kotlin/kotlinx.coroutines/blob/1.9.0/LICENSE.txt) |
| NanoHTTPD | Local phone setup server | [BSD 3-Clause license](https://github.com/NanoHttpd/nanohttpd/blob/nanohttpd-project-2.3.1/LICENSE.md) |
| ZXing Core | Setup QR code generation | [Apache License 2.0](https://github.com/zxing/zxing/blob/zxing-3.5.3/LICENSE) |

Before distributing an APK, review its resolved dependencies and include the applicable license texts, copyright notices, and any required NOTICE files with the distribution.

## Code of Conduct

[CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) uses [Contributor Covenant 2.1](https://www.contributor-covenant.org/version/2/1/code_of_conduct/), from [upstream revision 62ed665](https://github.com/EthicalSource/contributor_covenant/tree/62ed6650c053e8fc87ec096727147cbd218a8ac3), under [Creative Commons Attribution 4.0 International](https://github.com/EthicalSource/contributor_covenant/blob/62ed6650c053e8fc87ec096727147cbd218a8ac3/LICENSE.md). Its attribution and source links are retained. The reporting instructions use GitHub issues and explain that reports are public. An explicit license note has been added.
