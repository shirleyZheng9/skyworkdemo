---
name: web-access
description:
  所有联网与浏览器自动化应遵循本技能：轻量检索用 web_search/web_fetch/curl；需要动态页面、登录态、复杂交互时在已开启沙箱的会话中调用 browser_use（Playwright）。
  触发场景：搜索、读页、需登录网站、页面操作、反爬站点、动态渲染、以及任何需要真实浏览器环境的任务。
---

# 联网与浏览器技能（web-access）

## 前置条件

- **沙箱已开启**：`browser_use` 仅在 `ToolContext.sandboxEnabled() == true` 时可用；未开启时提示用户为该会话开启沙箱。
- **实现位置**：`bote-service` 的 `BrowserTools` → 沙箱内执行 `python3 /opt/bote/browser_runner.py`；脚本来源 `bote-sandbox/src/main/resources/browser/browser_runner.py`（首次可自动写入沙箱）。

## 浏览哲学

**像人一样思考，兼顾高效与适应性。**

**① 拿到请求** — 明确成功标准：要拿到什么信息、完成什么操作？

**② 选择起点** — 能静态解决的先用 WebSearch / WebFetch；需要交互、登录、强反爬或动态站 → **browser_use**（沙箱内 Playwright）。

**③ 过程校验** — 用每步结果对照成功标准；方向错就换路径，不在同一手段上无效重试。

**④ 完成判断** — 达标即停，避免为「完整」而过度操作。

## 联网工具选择

| 场景 | 工具                             |
|------|--------------------------------|
| 搜索摘要、发现来源 | **web-search**                 |
| URL 已知，需按语义抽取 | **web-fetch**                  |
| 需要原始 HTML（meta、JSON-LD 等） | **curl**                       |
| 强反爬、需登录、交互、动态渲染 | **browser_use**（沙箱 Playwright） |

`browser_use` 不要求事先知道最终 URL——可从入口页再通过 snapshot/click 导航。

## browser_use 工作流（核心）

工具名：**`browser_use`**。参数为 JSON（代码里 `BrowserUseRequest`，字段 snake_case），至少包含 **`action`**。

推荐闭环：

1. **`start`** — 启动浏览器会话（按需 `headed: true` 需结合环境是否支持 headed）。
2. **`open`** — `url` 打开页面；或 **`navigate`** 在当前页跳转。
3. **`snapshot`** — 获取可交互树与 **`ref`**（如 `e12`）；后续 **`click` / `type` / `hover`** 优先用 **ref**（与 snapshot 一致），iframe 用 **`frame_selector`**。
4. **`screenshot`** / **`eval`** / **`evaluate`** — 读图、跑页面级或元素级 JS。
5. **`tabs`** — `tab_action`: `list` | `new` | `close` | `select`；多页任务用新 tab，结束 **`close`** 自己开的页。
6. 原生弹窗阻塞时：**`handle_dialog`**（`accept` / `prompt_text`）。

常用 **action** 一览（与 `browser_runner.py` 一致）：

`start`, `stop`, `restart_server`, `open`, `navigate`, `navigate_back`, `snapshot`, `screenshot`, `click`, `type`, `file_upload`, `handle_dialog`, `hover`, `select_option`, `fill_form`, `drag`, `run_code`, `install`, `console_messages`, `network_requests`, `eval`, `evaluate`, `resize`, `press_key`, `tabs`, `wait_for`, `pdf`, `close`。

`action` 与字段细节见 **`references/browser-use-api.md`**。

内部诊断：**`get_connection_mode`**（由 `get_vnc_url` 等间接使用）返回 `connection_mode`：`cdp` 或 `launch`。

### 与「本机 CDP」文档的对应关系

- 上游 skill 里的 `/eval` ≈ **`eval`**（页面级表达式）或 **`evaluate`**（可选 `ref` + `frame_selector` 在元素上执行）。
- `/click` ≈ **`click`**（`selector` 或 **`ref`**）。
- `/new` ≈ **`tabs`** + `tab_action: new` 配合 **`open`**。
- `/scroll` — 用 **`eval`** 执行 `window.scrollBy` 或 Playwright 可接受的方式（以 snapshot 后页面结构为准）。

### 登录与 Cookie

沙箱内是 **独立浏览器环境**，**不会**自动带上用户本机 Chrome 的登录态。若任务依赖登录：

- 在沙箱会话内完成登录（扫码/账号密码视平台而定），或
- 由平台/运维配置 **CDP 接入** 等能力（见 `connection_mode` / 部署说明），再按环境要求操作。

仅在确认「未登录导致拿不到目标内容」时提示用户协助登录或配置环境。

### VNC 调试

当连接模式为 **`cdp`** 时，可调用 **`get_vnc_url`** 取得 noVNC 地址，便于人工查看沙箱内浏览器（具体 URL 由服务端 `BOTE_VNC_*`、`BOTE_SANDBOX_ID` 等拼装）。

### 文件上传

路径为 **沙箱内路径**：先点击上传控件，再 **`file_upload`** + **`paths_json`**（JSON 数组）。

## 程序化操作 vs GUI

- **程序化**（直接 `open` URL、`eval` 改 DOM）：快，但易被风控。
- **GUI**（snapshot 后按 ref **click/type**）：更接近真实用户，适合反爬站点。

站点内链接尽量从 **snapshot 里取完整 href**，避免手搓 URL 缺参数。

## 并行分治

多目标彼此独立时，可拆给子 Agent 并行；每个子任务在沙箱内自管 **`tabs`** / **`open`** / **`close`**，并在子任务说明中要求 **遵循本技能** 与 **`browser_use` 流程**。

主 Agent 描述**目标**（获取、调研、核对），少用会误导手段的词；子 Agent 自行选择 WebSearch 还是 browser_use。

## 信息核实

搜索引擎用于**发现**线索，核实要靠**一手来源**（官网、原文、官方文档）。与上游 skill 一致。

## 站点经验

按域名维护在 **`references/site-patterns/`**（相对本技能目录）。接单前若存在对应 `{domain}.md` 应先读；任务中验证过的新模式可追加（只写事实）。

模板：

```markdown
---
domain: example.com
aliases: [示例]
updated: 2026-03-31
---
## 平台特征
## 有效模式
## 已知陷阱
```
