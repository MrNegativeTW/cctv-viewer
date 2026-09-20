## PR Title 格式

注意：用畢請將此段刪除

建議使用 Conventional Commits：`<type>(<scope>): <description>`

範例：
- `feat(auth): 新增 Google OAuth 登入`
- `fix(api): 修正 access token 過期後的更新流程`
- `refactor(database): 重構 transaction handling`
- `perf(query): 降低使用者資料查詢次數`
- `ci(release): 新增自動發布流程`
- `chore(deps): 更新相依套件版本`

## 變更摘要

<!-- 請簡要說明此 PR 的目的、背景，以及主要變更內容。 -->

## 變更類型

<!-- 請勾選最符合此次變更的類型。 -->

- [ ] `feat` 新功能
- [ ] `fix` 錯誤修正
- [ ] `refactor` 程式碼重構
- [ ] `perf` 效能改善
- [ ] `docs` 文件更新
- [ ] `test` 測試相關
- [ ] `build` 建置系統或相依套件相關
- [ ] `ci` CI/CD 設定或流程變更
- [ ] `chore` 維護性調整
- [ ] `revert` 還原既有變更

## 主要變更

<!-- 請列出此次 PR 的重要修改項目。 -->

- 
- 
- 

## 相關 Issue

<!--
若此 PR 會關閉 Issue，可使用：
Closes #123
Fixes #123

若僅為關聯，可使用：
Related to #123
-->

Closes #

## 測試方式

<!-- 請描述如何驗證此 PR 的功能與正確性。 -->

- [ ] 單元測試
- [ ] 整合測試
- [ ] E2E 測試
- [ ] 手動測試
- [ ] 不適用

### 測試說明

<!--
請補充測試步驟、測試環境或特殊驗證方式。
-->

1. 
2. 
3. 

## 影響範圍

<!-- 請勾選此次變更可能影響的範圍。 -->

- [ ] 前端
- [ ] 後端
- [ ] API
- [ ] 資料庫
- [ ] CI/CD
- [ ] 基礎設施
- [ ] 相依套件
- [ ] 文件
- [ ] 無明顯影響

## Breaking Changes

- [ ] 此 PR 不包含 Breaking Change
- [ ] 此 PR 包含 Breaking Change

<!--
若包含 Breaking Change，請說明：
1. 哪些介面或行為發生改變
2. 受影響的使用者或系統
3. 升級或遷移方式
-->

## 畫面或輸出結果

<!--
若涉及 UI、CLI 輸出、API response 或其他可視化結果，
可在此附上截圖、影片或範例輸出。
-->

## 檢查清單

- [ ] PR Title 符合 Conventional Commits 格式
- [ ] 程式碼已完成自我檢查
- [ ] 已移除不必要的 debug code / log
- [ ] 已新增或更新必要測試
- [ ] 所有既有測試皆通過
- [ ] 已更新必要文件
- [ ] 已確認沒有提交敏感資訊
- [ ] 已確認沒有非預期的 Breaking Change
- [ ] 此 PR 的變更範圍保持單一且明確