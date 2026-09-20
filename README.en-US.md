<div align="center">

<img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp" width="128" height="128" alt="CCTV Viewer app icon">

<h1>CCTV Viewer</h1>
<h4>Watch your home security cameras on your Android TV</h4>

<p><a href="README.md" lang="zh-TW">繁體中文</a> | <strong>English (US)</strong></p>

![Android TV](https://img.shields.io/badge/Android_TV-8.0%2B-3DDC84?style=flat-square)
![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?style=flat-square)
![Material 3](https://img.shields.io/badge/Compose_for_TV-Material_3-6750A4?style=flat-square)

<p>
  <a href="#preview">Preview</a> •
  <a href="#features">Features</a> •
  <a href="#configuration">Camera Setup</a> •
  <a href="#installation">Build and Install</a> •
  <a href="#credits">Credits</a> •
  <a href="#license">License</a>
</p>
</div>

## Preview

<img src="assets/preview-20260921.png" height="256" alt="CCTV Viewer app preview">

<a id="features"></a>

## Features

- Supports eight camera feeds.
- Configure camera URLs from your phone.
- Automatically refresh the page every 30 seconds.

<a id="configuration"></a>

## Camera Setup

Choose either method below.

### Set Up with Your Phone (Recommended)

1. Connect your phone and TV to the same network.
2. Select "Settings" at the far right of the camera list, or press the Menu button on your remote.
3. Scan the QR code on your TV with your phone to configure camera URLs.
4. The temporary link expires when you close the settings dialog on your TV or leave the app.

The setup service uses IPv4 and HTTP. The temporary link restricts access, but data is not encrypted in transit.

### Configure at Build Time

1. Start with the example file, [cameras.example.properties](cameras.example.properties):

    ```sh
    cp -n cameras.example.properties cameras.local.properties
    ```

2. Edit `cameras.local.properties` and enter names and web page URLs for all eight cameras. For example:

    ```properties
    camera.1.name=Camera 1
    camera.1.url=http://192.0.2.10/view?channel=1&mode=live
    ```

3. Notes:

   - URLs must use `http://` or `https://`. The app does not include an RTSP player.
   - The configuration file uses UTF-8, so you can enter Chinese characters directly. Values do not need quotation marks.
   - Keep `&` characters in URLs as they are; do not replace them with `&amp;`. Single quotes do not need escaping. Write any backslash in a value as `\\`.
   - JavaScript, web storage, and media autoplay are enabled. Camera web pages must display their feeds automatically.
   - HTTP connections are allowed, but invalid HTTPS certificates are not bypassed.

<a id="installation"></a>

## Build and Install

### Requirements

- Android TV running Android 8.0 (API 26) or later
- Android SDK 37
- JDK 21

Sync Gradle and select **Run 'app'**.

<a id="credits"></a>

## Credits

- App icon: [Flaticon security camera icon](https://www.flaticon.com/free-icon/security-camera_7144872)
- Font used in the TV banner: [justfont "jf open Huninn"](https://justfont.com/huninn/)

<a id="license"></a>

## License

[MIT License](LICENSE). Third-party materials retain their own licenses; see [Third-Party Notices](THIRD_PARTY_NOTICES.md).
