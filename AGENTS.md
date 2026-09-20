# AGENTS

## Documents

| Document | When to Read |
| --- | --- |
| [Software Architecture](docs/software-architecture.md) | Read when locating code or changing startup, playback, scheduling, or component responsibilities. |

## General Instructions

- Follow the existing architecture, patterns, and conventions. Before introducing a library, abstraction layer, or new pattern, check whether the project already has an established approach.
- Keep responsibilities clear and dependencies traceable. Follow the maintainability and readability principles in the documentation index.
- Update the relevant documentation whenever behavior, architecture, APIs, or development workflows change substantially. Keep details in their respective documents instead of continually expanding this guide.
- Keep commit and PR messages concise and professional, without emojis. Commits created with AI assistance must include an appropriate `Co-Authored-By` trailer.

## 輸出與文字規範

本節適用於 Agent 產生的 Code Review、Commit Message、Pull Request、Issue、變更摘要、技術說明及文件內容。

### 基本原則

- 只輸出與目前任務直接相關的資訊。
- 同一事實只陳述一次。
- 不重複結論。
- 不輸出思考過程。
- 不輸出與結果無關的操作過程。
- 不為顯示工作量而列出工具、搜尋步驟、命令順序或中間檢查結果。
- 工具名稱僅在其本身構成必要驗證證據時保留。
- 能以一句完整句子表達時，不拆成多個 bullet。
- 若某項資訊刪除後，不影響讀者理解目前狀態、風險、驗證結果或下一步行動，應省略。

### 中文語體

- 使用 zh-Hant。
- 採正式、客觀、精確的技術文件語體。
- 使用自然中文語序。
- 避免翻譯腔與不自然的中英混接。
- 不使用口語化工程用語。
- 不使用無明確技術定義的形容詞。
- 禁止 emoji。
- 禁止比喻、擬人、戲劇化形容、哲學評論、情緒化語氣與誇張詞。
- 禁止以「漂亮」、「優雅」、「穩健」、「乾淨」等主觀形容詞描述程式碼。
- 避免使用「強化」、「防禦性」、「硬化」等語意不明確的詞彙，除非已有明確技術定義。
- 英文技術名詞僅在業界通常直接使用英文時保留，例如 API、PR、commit、branch、migration、FK。
- 程式碼識別字、函式名稱、欄位名稱、命令、檔名與 commit SHA 保留原文。
- 相同概念在同一回答中使用一致名稱。

避免：

```text
reviewer 已機械改號到 316
migration 鏈全綠
親驗沒有問題
單 bump backend version
```

應寫為：

```text
migration 編號已由 315 調整為 316。
完整 migration 測試通過。
已確認未發現異常。
僅更新後端版本號。
```

### 技術描述

描述程式碼、repository 與測試的客觀狀態，不以 Agent 自身行為作為主要敘事。

避免：

```text
我 grep 了整個 repository，確認沒有殘留。
```

應寫為：

```text
repository 內未發現對舊編號的殘留引用。
```

避免：

```text
我跑了 build、test、lint，全部綠了。
```

應寫為：

```text
建置、測試與格式檢查均通過。
```

避免：

```text
reviewer 把四個 migration 檔案機械改號，內容 byte-identical。
```

應寫為：

```text
四個 migration 檔案僅調整編號，內容未修改。
```

### 資訊准入條件

輸出只保留會影響下列事項的資訊：

- 是否可以繼續實作。
- 是否可以合併。
- 是否存在 blocker。
- 是否需要修改。
- 是否需要人工確認。
- 是否存在已知風險。
- 程式碼或資料狀態是否正確。
- 測試結果是否足以支持結論。

以下資訊預設省略：

- 使用了哪些工具。
- 命令執行順序。
- 搜尋過哪些檔案。
- 中間推理與嘗試過程。
- 已在其他段落出現的事實。
- 不影響結論的測試數量。
- 不影響結論的執行時間。
- 僅用於表示工作量的操作細節。

### 長度限制

除非任務明確要求完整報告：

- 一般技術摘要控制在 300–500 個中文字內。
- 無 blocker 的 Code Review 原則上不超過 300 個中文字。
- 每個 bullet 原則上不超過 2 句。
- 每個區段只保留必要項目。
- 新增內容前，確認該內容是否會改變讀者的判斷或下一步行動；若不會，省略。

### Code Review

Code Review 預設只使用以下區段：

```markdown
## 結論

- 最多 3 項。
- 僅列是否可合併、blocker、必要修改或重要狀態。

## 需要作者處理

- 僅列作者必須確認或執行的事項。
- 沒有事項時寫「無」。

## 驗證

- 最多 5 項。
- 只列足以支持結論的重要證據。
- 不重述「結論」。
```

除非任務明確要求，不建立以下區段：

- `我們的 Review`
- `使用的工具`
- `處理過程`
- `思考過程`
- `執行紀錄`
- `Advisory`
- `額外觀察`

若存在重要但非 blocker 的事項，放入既有區段並說明其影響。

### Code Review 用語

避免下列非正式或語意不精確的用語：

| 避免 | 使用 |
| --- | --- |
| 撞號 | 編號衝突 |
| 機械改號 | 調整編號 |
| 親驗 | 已驗證 |
| 綠了 / 全綠 | 測試通過 |
| 單 bump | 僅更新版本 |
| bump version | 更新版本號 |
| migration 鏈 | 完整 migration 序列或完整 migration 測試 |
| 裸插入 | 直接使用 `INSERT`，未包含冪等條件 |
| byte-identical | 內容完全一致；必要時使用「位元組內容一致」 |
| grep 全庫 | 已搜尋整個 repository |
| 沒碰到 | 未修改 |

替代方式依實際語意調整，不將表格內容視為固定模板。

### 文件產生

Agent 不得以「完整性」為理由產生大量重複文件內容。

設計文件優先包含：

1. 目的。
2. 範圍。
3. 主要設計。
4. 資料流或控制流程。
5. 關鍵介面。
6. 限制與已知風險。

除非任務明確要求，禁止加入：

- 長篇背景介紹。
- 通用程式設計知識。
- 大量最佳實務說明。
- Agent 工作紀錄。
- 對既有程式碼逐行重新描述。
- 為增加篇幅而加入的範例。

### 輸出前刪減

輸出前執行一次刪減：

1. 刪除重複資訊。
2. 刪除不影響判斷的操作細節。
3. 刪除沒有明確用途的背景說明。
4. 合併語意相同的句子與 bullet。
5. 將口語、翻譯腔與不自然中英混接改寫為書面中文。
6. 確認每個段落均提供新的資訊。
7. 確認內容未超出任務所需範圍。
