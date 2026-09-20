# 軟體架構

採用 ViewModel 與單向資料流。`CameraRepository` 以 Flow 提供攝影機設定，`CameraViewModel` 以 `StateFlow<CameraUiState>` 提供狀態，畫面透過 `CameraAction` 回報操作。

```mermaid
flowchart LR
    Config["cameras.local.properties"] --> Gradle["Gradle / BuildConfig"]
    Gradle --> Defaults["BuildConfigCameraDefaults"]
    Defaults --> Repository["DataStoreCameraRepository"]
    Phone["手機瀏覽器"] --> Server["CameraConfigServer"]
    Server -->|驗證後儲存| Store["DataStore"]
    Store -->|Flow| Repository
    Repository --> VM["CameraViewModel"]
    VM -->|CameraUiState| Route["CameraRoute"]
    Route --> Screen["CameraScreen"]
    Screen -->|CameraAction| VM
    Screen --> Bar["CameraSelectionBar"]
    Screen --> Web["CameraWebView / CameraPageView"]
```

| 位置 | 用途 |
| --- | --- |
| `data/` | 攝影機資料模型、設定驗證、BuildConfig 初始值及 DataStore 持久化。 |
| `network/` | 區網位址選擇、限此次連結存取的 HTTP 服務與手機表單。 |
| `i18n/` | 手機瀏覽器語言選擇與共用 Android 字串資源的轉接。 |
| `ui/activity/` | 應用程式入口、視窗設定及相依物件建立。 |
| `ui/viewmodel/` | 選取狀態、設定資料收集、設定視窗、重新載入指令及 10 秒閒置計時。 |
| `ui/screen/` | 生命週期感知的狀態收集、遙控器事件轉換與畫面組合。 |
| `ui/component/` | 清單動畫、焦點、錯誤提示、網頁生命週期及手機設定 QR Code 視窗。 |
| `ui/web/` | 原生網頁設定、載入回呼、載入狀態與資源釋放。 |
| `ui/theme/` | Material 3 深色主題。 |
| `ui/preview/` | 不含私人網址的 Compose 預覽示範資料。 |
| `cameras.example.properties` | 可提交的攝影機設定範例。 |
| `cameras.local.properties` | 已忽略的本機私人設定。 |
| `app/build.gradle.kts` | 讀取設定、處理字串跳脫並產生 BuildConfig。 |

程式碼套件位於 `app/src/main/java/com/txwstudio/app/cctvviewer/`，資源位於 `app/src/main/res/`。

ViewModel 使用 `SavedStateHandle` 保存目前攝影機、焦點項目及選單顯示狀態。原生網頁由介面層建立及銷毀，不存入 ViewModel；進入背景時仍會立即釋放，回到前景後重建。動畫、焦點及網頁載入進度屬於介面狀態，由對應元件管理。

ViewModel 單元測試位於 `app/src/test/java/com/txwstudio/app/cctvviewer/ui/viewmodel/`，使用替代資料來源與協程虛擬時間，驗證選取流程、狀態恢復及閒置計時，不會連線至攝影機。

## 手機設定

`CameraSettingsDialog` 開啟時，依電視的 Wi-Fi 或乙太網路介面取得 IPv4 位址，並在該位址啟動 `CameraConfigServer`。系統配置可用的連接埠，每次產生新的隨機存取路徑；關閉視窗或生命週期進入背景時停止服務，返回前景則產生新的連結。

手機頁面使用伺服器產生的 HTML 表單，不依賴外部網站、JavaScript 或雲端服務。GET 讀取目前設定，POST 驗證完整的八組資料後，以 DataStore 交易一次儲存；成功後重新導向確認頁。伺服器限制內容大小，拒絕不同來源的表單提交，並禁止快取。HTTP 本身不提供傳輸加密。

`DataStoreCameraRepository` 只在尚未儲存設定時採用建置初始值。ViewModel 收到設定變更後保留目前選取狀態；只有作用中攝影機的網址變更才增加 `reloadVersion`。設定視窗開啟時暫停閒置計時，關閉後重新計時。儲存內容位於應用程式私有儲存空間，不會寫回 properties 或原始碼。

DataStore 持久化測試位於 `app/src/test/java/com/txwstudio/app/cctvviewer/data/`；HTTP 測試位於 `app/src/test/java/com/txwstudio/app/cctvviewer/network/`，使用 loopback 連線與示範資料驗證存取限制、字串跳脫、輸入驗證與儲存行為。

## 多語系

電視介面與手機設定頁共用 `res/values*/strings.xml`，包括按鈕、表單、成功提示與驗證錯誤。預設資源為臺灣繁體中文；另外提供明確的 `values-zh-rTW`，避免繁體中文裝置誤用簡體中文資源。

| 語言 | 語系代碼 | 資源目錄 |
| --- | --- | --- |
| 繁體中文（臺灣，預設） | `zh-TW` | `values`、`values-zh-rTW` |
| 簡體中文 | `zh-CN` | `values-zh-rCN` |
| 英文（美國） | `en-US` | `values-en` |
| 日文 | `ja-JP` | `values-ja` |
| 韓文 | `ko-KR` | `values-ko` |

`locales_config.xml` 宣告支援語系，供 Android 13 以上支援應用程式語言設定的裝置使用。較舊版本依系統語言載入資源。

手機請求由 `AppLanguage` 解析 `Accept-Language` 的偏好順序及權重，不支援或格式錯誤時回退繁體中文。`AndroidWebStrings` 以獨立資源設定讀取翻譯，不改變電視的語言。HTTP 回應包含 `Content-Language` 與 `Vary: Accept-Language`；所有翻譯都會一起打包，確保手機可使用與電視不同的語言。

資料層只回傳結構化的 `CameraValidationException`，由網頁呈現層轉成該請求的語言。自訂攝影機名稱與網址維持原值；空白名稱在介面顯示時使用翻譯，不寫入語言相關的預設名稱。

語言選擇、翻譯完整性與格式參數測試位於 `app/src/test/java/com/txwstudio/app/cctvviewer/i18n/`。HTTP 測試涵蓋五種語言的表單、成功訊息、驗證錯誤及預設語言回退。

## Compose 預覽

在 Android Studio 開啟下列 Kotlin 檔案，切換至設計或分割檢視，即可查看預覽。完整觀看畫面使用 960 × 540 dp 的橫向尺寸；觀看畫面與手機設定視窗透過 `@CameraLocalesPreview` 同時提供五種語言，其餘元件維持繁體中文預覽。

| 檔案 | 預覽內容 |
| --- | --- |
| `ui/screen/CameraScreen.kt` | 顯示清單、隱藏清單的全畫面模式。 |
| `ui/component/CameraSelectionBar.kt` | 橫向攝影機清單，區分正在觀看與聚焦的項目。 |
| `ui/component/CameraButton.kt` | 一般及已選取按鈕。 |
| `ui/component/CameraWebView.kt` | 攝影機網頁區域的示意畫面。 |
| `ui/component/CameraPageOverlay.kt` | 載入進度、尚未設定、載入失敗及憑證錯誤。 |
| `ui/component/CameraSettingsDialog.kt` | QR Code、臨時網址與關閉按鈕。 |

預覽使用示範資料，不讀取私人設定或建立 ViewModel。設計預覽中的網頁區域透過 `LocalInspectionMode` 顯示示意區塊，不建立原生 WebView；實際網頁播放仍需在模擬器或電視上驗證。

[返回專案說明](../README.md)
