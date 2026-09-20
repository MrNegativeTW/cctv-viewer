<div align="center">

<img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp" width="128" height="128" alt="監視器應用程式圖示">

<h1>監視器</h1>
<h4>供個人使用的 Android TV 攝影機檢視工具</h4>

![Android TV](https://img.shields.io/badge/Android_TV-8.0%2B-3DDC84?style=flat-square)
![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?style=flat-square)
![Material 3](https://img.shields.io/badge/Compose_for_TV-Material_3-6750A4?style=flat-square)

<p>
  <a href="#preview">Preview</a> •
  <a href="#features">功能</a> •
  <a href="#configuration">攝影機設定</a> •
  <a href="#installation">建置與安裝</a> •
  <a href="#credits">素材來源</a> •
  <a href="#license">授權</a>
</p>
</div>

## Preview

<img src="assets/preview-20260921.png" height="256" alt="監視器應用程式圖示">

<a id="features"></a>

## 功能

- 在你家電視上看你家監視器畫面

本專案供安裝至自有電視使用，不以應用程式商店發布為目標。

<a id="configuration"></a>

## 攝影機設定

首次設定時，在專案根目錄複製 [cameras.example.properties](cameras.example.properties)：

```sh
cp -n cameras.example.properties cameras.local.properties
```

編輯 `cameras.local.properties`，填入八組名稱與網頁網址。以下網址僅供示意：

```properties
camera.1.name=攝影機 1
camera.1.url=http://192.0.2.10/view?channel=1&mode=live
```

`cameras.local.properties` 已加入 `.gitignore`，只供本機使用。`local.properties` 則保留給 Android Studio 管理 SDK 路徑。

Android Studio 與命令列建置都會透過 Gradle 讀取本機設定，產生 `BuildConfig`，由 `BuildConfigCameraRepository` 提供給應用程式。修改後重新同步或建置並安裝即可，不需編輯 XML 或 Kotlin 原始碼。

第一組設定就是啟動時載入的攝影機。沒有本機設定檔時仍可建置，預設顯示八個通用名稱及空白網址；缺少名稱也會使用通用名稱，網址留空則顯示尚未設定的訊息。

- 網址須使用 `http://` 或 `https://`。應用程式未內建 RTSP 播放器。
- 設定檔使用 UTF-8，可直接填入中文；值不需要加引號。
- 網址中的 `&` 直接保留，不要寫成 `&amp;`；單引號不需跳脫。若值包含反斜線，請寫成 `\\`。
- 電視須能連線至攝影機所在的網路。
- 已啟用網頁指令碼、網頁儲存空間與媒體自動播放；攝影機網頁須能自動顯示影像。
- 允許 HTTP 連線，但不會略過無效的 HTTPS 安全性憑證。

攝影機網頁的內容與語言由攝影機本身提供。

<a id="installation"></a>

## 建置與安裝

### 環境需求

- Android TV，Android 8.0（API 26）以上版本
- Android SDK 37
- JDK 21，依專案的 Gradle 工具鏈設定使用

以 Android Studio 開啟專案並完成 Gradle 同步後選擇 Run 'app'

<a id="credits"></a>

## 素材來源

- 應用程式圖示：[Flaticon 監視攝影機圖示](https://www.flaticon.com/free-icon/security-camera_7144872)
- 電視橫幅圖片使用字型：[justfont「jf open 粉圓」](https://justfont.com/huninn/)

<a id="license"></a>

## 授權

尚未決定
