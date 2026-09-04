# `browser_use` / Playwright 参考

实现：`bote-sandbox/src/main/resources/browser/browser_runner.py`，由 `BrowserTools.browserUse` 在沙箱内调用。

## 约束

- 必须通过 **`browser_use` 工具** 调用，参数为 JSON；字段名与下表一致（snake_case）。
- 浏览器在 **沙箱** 中运行，与开发者本机 Chrome / `localhost:3456` CDP Proxy **无关**。

## 通用字段

| 字段 | 说明 |
|------|------|
| `action` | 必填，见下表 |
| `page_id` | 页/标签 id，默认 `default`；`open`/`tabs` 可能返回 `page_1` 等 |
| `url` | `open` / `navigate` 使用 |
| `selector` | CSS 选择器 |
| `ref` | snapshot 中的元素引用，优先于裸 selector |
| `text` | `type` 的文本 |
| `code` | `eval` / `evaluate` / `run_code` 的 JS |
| `path` | 截图、PDF、导出路径等 |
| `frame_selector` | iframe，如 `iframe#main` |
| `full_page` | 整页截图 |
| `wait` | 点击后等待毫秒 |
| `tab_action` | `tabs` 用：`list` / `new` / `close` / `select` |
| `index` | `tabs` 关闭或选中时用 |
| `paths_json` | `file_upload`：沙箱内文件路径 JSON 数组 |
| `fields_json` | `fill_form`：字段数组 JSON |
| `accept` / `prompt_text` | `handle_dialog` |

完整列表以 `BrowserTools.BrowserUseRequest` 与 `browser_runner.py` 中 `dispatch` 为准。

## Action 速查

| action | 作用 |
|--------|------|
| `start` | 启动会话；可选 `headed` |
| `stop` | 停止浏览器 |
| `restart_server` | 重启沙箱内 browser server |
| `open` | 新开/导航到 `url` |
| `navigate` / `navigate_back` | 导航与后退 |
| `snapshot` | 可访问性树 + ref；可选 `snapshot_filename` |
| `screenshot` | 截图；可选 `ref`、`full_page`、`screenshot_type` |
| `click` | 点击；`ref` 或 `selector` |
| `type` | 输入；可选 `submit`、`slowly` |
| `file_upload` | 上传（沙箱路径） |
| `handle_dialog` | 处理 alert/confirm/prompt |
| `hover` / `select_option` / `fill_form` / `drag` | 交互 |
| `eval` | 页面级 JS |
| `evaluate` | 元素级 JS（配合 `ref`、`frame_selector`） |
| `run_code` | 在 page 上执行较长脚本 |
| `console_messages` / `network_requests` | 调试输出 |
| `press_key` / `resize` / `wait_for` | 键盘、视口、等待 |
| `tabs` | 多标签 |
| `pdf` | 导出 PDF |
| `close` | 关闭页面/会话相关逻辑（以实现对称为准） |
| `install` | 尝试 `playwright install chromium` |
| `get_connection_mode` | 返回 `connection_mode`（供 `get_vnc_url` 等使用） |

## 典型顺序

```
start → open(url) → snapshot → click(ref=...) / type(...) → snapshot → …
```

弹窗阻塞 snapshot/screenshot 时：**先 `handle_dialog`**，再继续。
