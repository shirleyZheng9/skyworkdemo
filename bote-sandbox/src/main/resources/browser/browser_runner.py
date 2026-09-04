#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Browser runner for sandbox: runs Playwright browser actions.
Usage: python3 browser_runner.py --user-id <userId> --params-base64 <base64_json>
When first run for a user, starts a background server that keeps browser state;
subsequent runs send the action to the server and print JSON result to stdout.
Requires: pip install playwright && playwright install chromium
"""

import argparse
import asyncio
import base64
import json
import os
import socket
import subprocess
import sys
import time
import re
import urllib.request
from typing import Any

SOCKET_DIR = "/tmp"
PID_DIR = "/tmp"


def get_socket_path(user_id):
    return os.path.join(SOCKET_DIR, "bote_browser_{}.sock".format(user_id))


def get_pid_path(user_id):
    return os.path.join(PID_DIR, "bote_browser_{}.pid".format(user_id))


def is_server_running(user_id):
    pid_path = get_pid_path(user_id)
    if not os.path.exists(pid_path):
        return False
    try:
        with open(pid_path) as f:
            pid = int(f.read().strip())
        os.kill(pid, 0)
        return True
    except (ValueError, OSError):
        return False


def _cleanup_server(user_id):
    """清理该用户的 server 占位文件，便于下次重新启动 server。"""
    for path in (get_socket_path(user_id), get_pid_path(user_id)):
        try:
            if os.path.exists(path):
                os.unlink(path)
        except OSError:
            pass


def start_server(user_id):
    """Start browser server in background for this user."""
    script_dir = os.path.dirname(os.path.abspath(__file__))
    script = os.path.join(script_dir, "browser_runner.py")
    env = os.environ.copy()
    env["BOTE_BROWSER_SERVER"] = "1"
    env["BOTE_BROWSER_USER_ID"] = str(user_id)
    proc = subprocess.Popen(
        [sys.executable, script, "--user-id", str(user_id)],
        env=env,
        stdout=subprocess.DEVNULL,
        stderr=subprocess.DEVNULL,
        start_new_session=True,
    )
    pid_path = get_pid_path(user_id)
    with open(pid_path, "w") as f:
        f.write(str(proc.pid))
    sock_path = get_socket_path(user_id)
    for _ in range(50):
        time.sleep(0.1)
        if os.path.exists(sock_path):
            return True
    return False


def _exit_with_status_from_json(result: str) -> None:
    """失败时非零退出，便于沙箱层用 exit_code 判定，而不是仅依赖 stdout 里的 ok: false。"""
    if not result or not result.strip():
        sys.exit(1)
    try:
        obj = json.loads(result)
    except Exception:
        sys.exit(1)
    if isinstance(obj, dict) and obj.get("ok") is False:
        sys.exit(1)


def send_request(user_id, params):
    """Send request to server and return response JSON string."""
    sock_path = get_socket_path(user_id)
    if not os.path.exists(sock_path):
        return json.dumps({"ok": False, "error": "Browser server not running"})
    try:
        sock = socket.socket(socket.AF_UNIX, socket.SOCK_STREAM)
        sock.settimeout(120)
        sock.connect(sock_path)
        payload = json.dumps(params, ensure_ascii=False)
        msg = payload.encode("utf-8") + b"\n"
        sock.sendall(msg)
        buf = b""
        while True:
            chunk = sock.recv(4096)
            if not chunk:
                break
            buf += chunk
            if b"\n" in buf:
                break
        sock.close()
        return buf.decode("utf-8").strip().split("\n")[0]
    except Exception as e:
        return json.dumps({"ok": False, "error": str(e)}, ensure_ascii=False)


def run_client(user_id, params):
    if not is_server_running(user_id):
        if not start_server(user_id):
            out = json.dumps({"ok": False, "error": "Failed to start browser server"}, ensure_ascii=False)
            print(out)
            sys.exit(1)
    result = send_request(user_id, params)
    # 若返回连接错误，清理陈旧 pid/socket 后重试一次
    if result and "Connection refused" in result:
        _cleanup_server(user_id)
        if not is_server_running(user_id):
            start_server(user_id)
        result = send_request(user_id, params)
    print(result)
    _exit_with_status_from_json(result)


# --- Server side: Playwright state and action handlers ---

_state = {
    "playwright": None,
    "browser": None,
    "context": None,
    "pages": {},
    "refs": {},
    "refs_frame": {},  # page_id -> frame_selector used in last snapshot (for click/type in iframe)
    "headless": True,
    "_last_browser_error": None,
    "pending_file_choosers": {},
    "pending_dialogs": {},
    "console_logs": {},
    "network_requests": {},
    "current_page_id": None,
    "page_counter": 0,  # monotonic, for page_N ids (CoPaw-compatible)
    "connection_mode": "",  # cdp | launch
}

# Chromium 在沙箱/容器中需要这些参数，否则易启动失败
_CHROMIUM_ARGS = ["--no-sandbox", "--disable-dev-shm-usage", "--disable-gpu", "--single-process"]


def _fetch_cdp_ws_url(port: int, timeout_sec: float = 3.0) -> str:
    """Try read local CDP websocket URL from /json/version."""
    deadline = time.time() + max(0.2, float(timeout_sec))
    url = "http://127.0.0.1:{}/json/version".format(int(port))
    while time.time() < deadline:
        try:
            with urllib.request.urlopen(url, timeout=1.0) as resp:
                data = json.loads(resp.read().decode("utf-8", errors="ignore"))
                ws = (data.get("webSocketDebuggerUrl") or "").strip()
                if ws:
                    return ws
        except Exception:
            pass
        time.sleep(0.1)
    return ""


# --- Snapshot helpers (ported from CoPaw browser_snapshot) ---

INTERACTIVE_ROLES = frozenset(
    {
        "button",
        "link",
        "textbox",
        "checkbox",
        "radio",
        "combobox",
        "listbox",
        "menuitem",
        "menuitemcheckbox",
        "menuitemradio",
        "option",
        "searchbox",
        "slider",
        "spinbutton",
        "switch",
        "tab",
        "treeitem",
    },
)

CONTENT_ROLES = frozenset(
    {
        "heading",
        "cell",
        "gridcell",
        "columnheader",
        "rowheader",
        "listitem",
        "article",
        "region",
        "main",
        "navigation",
    },
)

STRUCTURAL_ROLES = frozenset(
    {
        "generic",
        "group",
        "list",
        "table",
        "row",
        "rowgroup",
        "grid",
        "treegrid",
        "menu",
        "menubar",
        "toolbar",
        "tablist",
        "tree",
        "directory",
        "document",
        "application",
        "presentation",
        "none",
    },
)


def _get_indent_level(line: str) -> int:
    m = re.match(r"^(\s*)", line)
    return int(len(m.group(1)) / 2) if m else 0


def _create_tracker() -> dict[str, Any]:
    counts: dict[str, int] = {}
    refs_by_key: dict[str, list[str]] = {}

    def get_key(role: str, name: str | None) -> str:
        return f"{role}:{name or ''}"

    def get_next_index(role: str, name: str | None) -> int:
        key = get_key(role, name)
        current = counts.get(key, 0)
        counts[key] = current + 1
        return current

    def track_ref(role: str, name: str | None, ref: str) -> None:
        key = get_key(role, name)
        refs_by_key.setdefault(key, []).append(ref)

    def get_duplicate_keys() -> set[str]:
        return {k for k, refs in refs_by_key.items() if len(refs) > 1}

    return {
        "get_next_index": get_next_index,
        "track_ref": track_ref,
        "get_duplicate_keys": get_duplicate_keys,
        "get_key": get_key,
    }


def _remove_nth_from_non_duplicates(refs: dict[str, dict], tracker: dict) -> None:
    dup_keys = tracker["get_duplicate_keys"]()
    for _, data in list(refs.items()):
        role = data.get("role")
        # 对 textbox / searchbox 始终保留 nth，下次通过 ref 使用 role+nth 精确定位，
        # 避免因为 name（placeholder/value）变化导致定位错到其他输入框或超时。
        if role in ("textbox", "searchbox"):
            continue
        key = tracker["get_key"](role, data.get("name"))
        if key not in dup_keys and "nth" in data:
            del data["nth"]


def _compact_tree(tree: str) -> str:
    lines = tree.split("\n")
    result = []
    for i, line in enumerate(lines):
        if "[ref=" in line:
            result.append(line)
            continue
        if ":" in line and not line.rstrip().endswith(":"):
            result.append(line)
            continue
        current_indent = _get_indent_level(line)
        has_relevant = False
        for j in range(i + 1, len(lines)):
            if _get_indent_level(lines[j]) <= current_indent:
                break
            if "[ref=" in lines[j]:
                has_relevant = True
                break
        if has_relevant:
            result.append(line)
    return "\n".join(result)


def _process_line(  # pylint: disable=too-many-return-statements
    line: str,
    refs: dict[str, dict],
    options: dict[str, Any],
    tracker: dict,
    next_ref: Any,
) -> str | None:
    depth = _get_indent_level(line)
    max_depth_val = options.get("maxDepth")
    if max_depth_val is not None and depth > max_depth_val:
        return None

    m = re.match(r'^(\s*-\s*)(\w+)(?:\s+"([^"]*)")?(.*)$', line)
    if not m:
        return None if options.get("interactive") else line

    prefix, role_raw, name, suffix = m.groups()
    if role_raw.startswith("/"):
        return None if options.get("interactive") else line

    role = role_raw.lower()
    is_interactive = role in INTERACTIVE_ROLES
    is_content = role in CONTENT_ROLES
    is_structural = role in STRUCTURAL_ROLES

    if options.get("interactive") and not is_interactive:
        return None
    if options.get("compact") and is_structural and not name:
        return None

    should_have_ref = is_interactive or (is_content and name)
    if not should_have_ref:
        return line

    ref = next_ref()
    # 对 textbox / searchbox，我们希望 nth 是“该页面上第几个此类输入框”，
    # 而不是“同名元素中的第几个”，所以这里统一用 name=None 来计数。
    name_for_index = None if role in ("textbox", "searchbox") else name
    nth = tracker["get_next_index"](role, name_for_index)
    tracker["track_ref"](role, name_for_index, ref)
    refs[ref] = {"role": role, "name": name, "nth": nth}

    enhanced = f"{prefix}{role_raw}"
    if name:
        enhanced += f' "{name}"'
    enhanced += f" [ref={ref}]"
    if nth is not None and nth > 0:
        enhanced += f" [nth={nth}]"
    if suffix:
        enhanced += suffix
    return enhanced


def build_role_snapshot_from_aria(
    aria_snapshot: str,
    *,
    interactive: bool = False,
    compact: bool = False,
    max_depth: int | None = None,
) -> tuple[str, dict[str, dict]]:
    """Build snapshot + refs from Playwright locator.aria_snapshot() output."""
    options: dict[str, Any] = {
        "interactive": interactive,
        "compact": compact,
        "maxDepth": max_depth,
    }
    lines = aria_snapshot.split("\n")
    refs: dict[str, dict] = {}
    tracker = _create_tracker()
    counter = [0]

    def next_ref() -> str:
        counter[0] += 1
        return f"e{counter[0]}"

    result_lines = []
    for line in lines:
        processed = _process_line(line, refs, options, tracker, next_ref)
        if processed is not None:
            result_lines.append(processed)
    _remove_nth_from_non_duplicates(refs, tracker)
    tree = "\n".join(result_lines) or "(empty)"
    snapshot = _compact_tree(tree) if options.get("compact") else tree
    return snapshot, refs


async def ensure_browser(headless=True):
    if _state["browser"] is not None:
        return True
    _state["_last_browser_error"] = None
    try:
        from playwright.async_api import async_playwright
        pw = await async_playwright().start()
        _state["playwright"] = pw
        # 优先连接容器内已启动 Chromium（默认 9221，来自 start-chromium.sh）
        cdp_port = int(os.environ.get("BOTE_CDP_PORT", "9221") or 9221)
        cdp_ws = _fetch_cdp_ws_url(cdp_port, timeout_sec=3.0)
        if cdp_ws:
            _state["browser"] = await pw.chromium.connect_over_cdp(cdp_ws)
            _state["connection_mode"] = "cdp"
            contexts = _state["browser"].contexts
            if contexts:
                ctx = contexts[0]
            else:
                ctx = await _state["browser"].new_context(locale="zh-CN")
        else:
            # 回退：本地直接启动 Chromium
            launch_options = {"headless": headless, "args": _CHROMIUM_ARGS}
            # 若沙箱已配置系统 Chromium（如 PLAYWRIGHT_CHROMIUM_EXECUTABLE_PATH=/usr/lib64/chromium-browser/headless_shell），则使用该路径
            exe = os.environ.get("PLAYWRIGHT_CHROMIUM_EXECUTABLE_PATH", "").strip()
            if exe and os.path.isfile(exe):
                launch_options["executable_path"] = exe
            _state["browser"] = await pw.chromium.launch(**launch_options)
            _state["connection_mode"] = "launch"
            ctx = await _state["browser"].new_context(locale="zh-CN")
        _state["context"] = ctx

        def _on_context_page(page):
            """Auto-register new tab (e.g. target=_blank). CoPaw-compatible."""
            new_id = _next_page_id()
            _state["refs"][new_id] = {}
            _state["refs_frame"][new_id] = ""
            _state["pending_file_choosers"][new_id] = []
            _state["pending_dialogs"][new_id] = []
            _state["console_logs"][new_id] = []
            _state["network_requests"][new_id] = []
            choosers = _state["pending_file_choosers"][new_id]
            dialogs = _state["pending_dialogs"][new_id]

            def _on_fc(c):
                choosers.append(c)

            def _on_dlg(d):
                dialogs.append(d)

            def _on_con(msg):
                _state["console_logs"][new_id].append({"level": getattr(msg, "type", "log"), "text": getattr(msg, "text", "")})

            def _on_req(req):
                _state["network_requests"][new_id].append({"url": req.url, "method": req.method, "resourceType": getattr(req, "resource_type", None)})

            def _on_res(res):
                for r in _state["network_requests"][new_id]:
                    if r.get("url") == res.url and "status" not in r:
                        r["status"] = res.status
                        break

            try:
                page.on("filechooser", _on_fc)
                page.on("dialog", _on_dlg)
                page.on("console", _on_con)
                page.on("request", _on_req)
                page.on("response", _on_res)
                page.set_default_timeout(10000)
            except Exception:
                pass
            _state["pages"][new_id] = page
            _state["current_page_id"] = new_id

        try:
            ctx.on("page", _on_context_page)
        except Exception:
            pass
        return True
    except Exception as e:
        _state["_last_browser_error"] = str(e)
        return False


def _resolve_page_id(page_id: str) -> str:
    """解析 page_id：default 时优先用 current_page_id，否则若仅一页则用该页。"""
    page_id = (page_id or "default").strip() or "default"
    if _state["pages"].get(page_id) is not None:
        return page_id
    current = _state.get("current_page_id")
    pages = _state.get("pages") or {}
    if page_id == "default" and current and current in pages:
        return current
    ids = list(pages.keys())
    if page_id == "default" and len(ids) == 1:
        return ids[0]
    return page_id


def _next_page_id():
    """Return unique page_id (page_N). Monotonic, never reused after close."""
    _state["page_counter"] = _state.get("page_counter", 0) + 1
    return "page_{}".format(_state["page_counter"])


def get_page(page_id):
    resolved = _resolve_page_id(page_id)
    return _state["pages"].get(resolved)


def get_refs(page_id: str) -> dict:
    return _state["refs"].setdefault(page_id, {})


def _get_root(page, page_id: str, frame_selector: str = ""):
    """Return page or frame_locator for operating inside iframe. CoPaw-compatible."""
    if not (frame_selector and frame_selector.strip()):
        return page
    return page.frame_locator(frame_selector.strip())


def get_locator_by_ref(page, page_id: str, ref: str, frame_selector: str = ""):
    """Resolve snapshot ref to locator using ARIA role + name + nth.
    When frame_selector is set, locator is resolved inside that iframe.
    For textbox/searchbox we use role+nth only (no name)."""
    page_id = _resolve_page_id(page_id or "default")
    page_refs = get_refs(page_id)
    data = page_refs.get(ref)
    if not data:
        return None
    role = data.get("role", "generic")
    name = data.get("name") or None
    nth = data.get("nth", 0)
    root = _get_root(page, page_id, frame_selector)
    if role in ("textbox", "searchbox"):
        locator = root.get_by_role(role).nth(nth)
    else:
        locator = root.get_by_role(role, name=name)
        if nth is not None:
            locator = locator.nth(nth)
    return locator


async def action_start(headed=False):
    if _state["browser"] is not None:
        return {"ok": True, "message": "Browser already running", "connection_mode": _state.get("connection_mode") or "unknown"}
    _state["headless"] = not headed
    # 沙箱内通常无显示，headed 会失败，直接使用 headless
    if headed:
        _state["headless"] = True
    ok = await ensure_browser(_state["headless"])
    if not ok:
        err = _state.get("_last_browser_error") or ""
        if not err:
            err = (
                "Browser start failed. Try action=restart_server then action=start again to see the real error. "
                "Ensure Playwright is installed in the sandbox: pip install playwright && playwright install chromium"
            )
        return {"ok": False, "error": err}
    msg = "Browser started (visible window)" if not _state["headless"] else "Browser started"
    if headed and _state["headless"]:
        msg += " (headed not available in sandbox, using headless)"
    return {"ok": True, "message": msg, "connection_mode": _state.get("connection_mode") or "unknown"}


async def action_get_connection_mode():
    mode = (_state.get("connection_mode") or "").strip()
    running = _state.get("browser") is not None
    return {"ok": True, "browser_running": bool(running), "connection_mode": mode or "unknown"}


async def action_stop():
    if _state["browser"] is None:
        return {"ok": True, "message": "Browser not running"}
    try:
        await _state["browser"].close()
        if _state.get("playwright"):
            await _state["playwright"].stop()
    except Exception:
        pass
    _state["playwright"] = None
    _state["browser"] = None
    _state["context"] = None
    _state["pages"].clear()
    _state["refs"].clear()
    _state.get("refs_frame", {}).clear()
    _state.get("pending_file_choosers", {}).clear()
    _state.get("pending_dialogs", {}).clear()
    _state.get("console_logs", {}).clear()
    _state.get("network_requests", {}).clear()
    _state["current_page_id"] = None
    _state["connection_mode"] = ""
    return {"ok": True, "message": "Browser stopped"}


async def action_restart_server():
    """退出当前 server 进程，下次 browser_use 会启动新 server 并加载最新脚本。用于脚本更新后看到真实错误或修复问题。"""
    try:
        if _state["browser"] is not None:
            await _state["browser"].close()
            if _state.get("playwright"):
                await _state["playwright"].stop()
    except Exception:
        pass
    # 先返回响应，延迟后强制退出，使下次调用会启动新 server 加载最新脚本
    loop = asyncio.get_event_loop()
    loop.call_later(0.3, lambda: os._exit(0))
    return {"ok": True, "message": "Server will restart. Call browser_use (e.g. action=start) again to use the updated script."}


async def action_open(url, page_id="default"):
    if not url or not url.strip():
        return {"ok": False, "error": "url required for open"}
    ok = await ensure_browser(_state["headless"])
    if not ok:
        return {"ok": False, "error": "Browser not started"}
    try:
        page = await _state["context"].new_page()
        # 若 context.on("page") 已注册该页（如 new_page() 触发事件），则复用其 id，避免重复
        new_id = None
        for pid, p in (_state.get("pages") or {}).items():
            if p is page:
                new_id = pid
                break
        if new_id is None:
            new_id = _next_page_id()
            try:
                page.set_default_timeout(10000)
            except Exception:
                pass

            choosers = _state["pending_file_choosers"].setdefault(new_id, [])
            dialogs = _state["pending_dialogs"].setdefault(new_id, [])
            _state["console_logs"].setdefault(new_id, [])
            _state["network_requests"].setdefault(new_id, [])

            def on_filechooser(chooser):
                choosers.append(chooser)

            def on_dialog(dialog):
                dialogs.append(dialog)

            def on_console(msg):
                _state["console_logs"][new_id].append({"level": getattr(msg, "type", "log"), "text": getattr(msg, "text", "")})

            def on_request(req):
                _state["network_requests"][new_id].append({
                    "url": req.url,
                    "method": req.method,
                    "resourceType": getattr(req, "resource_type", None),
                })

            def on_response(res):
                for r in _state["network_requests"][new_id]:
                    if r.get("url") == res.url and "status" not in r:
                        r["status"] = res.status
                        break

            try:
                page.on("filechooser", on_filechooser)
                page.on("dialog", on_dialog)
                page.on("console", on_console)
                page.on("request", on_request)
                page.on("response", on_response)
            except Exception:
                pass

            _state["pages"][new_id] = page
            _state["refs"][new_id] = {}
            _state["refs_frame"][new_id] = ""

        await page.goto(url.strip())
        _state["current_page_id"] = new_id
        return {"ok": True, "message": "Opened " + url, "page_id": new_id, "url": url}
    except Exception as e:
        return {"ok": False, "error": str(e)}


async def action_navigate(url, page_id="default"):
    page = get_page(page_id)
    if not page:
        return {"ok": False, "error": "Page not found: " + page_id}
    if not url or not url.strip():
        return {"ok": False, "error": "url required for navigate"}
    try:
        await page.goto(url.strip())
        resolved = _resolve_page_id(page_id)
        _state["current_page_id"] = resolved
        return {"ok": True, "message": "Navigated to " + url, "url": page.url}
    except Exception as e:
        return {"ok": False, "error": str(e)}


async def action_snapshot(page_id="default", frame_selector="", snapshot_filename=""):
    page = get_page(page_id)
    if not page:
        return {"ok": False, "error": "Page not found: " + page_id}
    try:
        root = _get_root(page, page_id, frame_selector)
        locator = root.locator(":root") if hasattr(root, "locator") else page.locator(":root")
        raw = await locator.aria_snapshot()
        snapshot, refs = build_role_snapshot_from_aria(str(raw), interactive=False, compact=False, max_depth=None)
        _state["refs"][page_id] = refs
        _state["refs_frame"][page_id] = (frame_selector or "").strip()
        out = {"ok": True, "snapshot": snapshot, "refs": list(refs.keys()), "url": page.url, "page_id": page_id}
        if frame_selector and frame_selector.strip():
            out["frame_selector"] = frame_selector.strip()
        if snapshot_filename and snapshot_filename.strip():
            try:
                with open(snapshot_filename.strip(), "w", encoding="utf-8") as f:
                    f.write(snapshot)
                out["filename"] = snapshot_filename.strip()
            except Exception:
                pass
        return out
    except Exception as e:
        return {"ok": False, "error": str(e)}


async def action_screenshot(page_id="default", path=None, full_page=False, ref="", frame_selector="", screenshot_type="png"):
    page = get_page(page_id)
    if not page:
        return {"ok": False, "error": "Page not found: " + page_id}
    path = (path or "").strip() or "/tmp/screenshot_{}.png".format(int(time.time()))
    img_type = "jpeg" if (screenshot_type or "").lower() == "jpeg" else "png"
    if not path.endswith(".png") and not path.endswith(".jpeg") and not path.endswith(".jpg"):
        path = path + ("." + img_type)
    try:
        if ref and ref.strip():
            locator = get_locator_by_ref(page, page_id, ref.strip(), frame_selector)
            if locator:
                await locator.screenshot(path=path, type=img_type)
            else:
                await page.screenshot(path=path, full_page=bool(full_page), type=img_type)
        elif frame_selector and frame_selector.strip():
            root = _get_root(page, page_id, frame_selector)
            locator = root.locator("body").first if hasattr(root, "locator") else page.locator("body").first
            await locator.screenshot(path=path, type=img_type)
        else:
            await page.screenshot(path=path, full_page=bool(full_page), type=img_type)
        return {"ok": True, "message": "Screenshot saved", "path": path}
    except Exception as e:
        return {"ok": False, "error": str(e)}


async def _collect_new_tabs_after_click():
    """点击后若有新打开的标签页，登记到 _state 并返回其 page_id 列表。"""
    ctx = _state.get("context")
    if not ctx:
        return []
    known = set(_state["pages"].values())
    await asyncio.sleep(0.4)
    new_ids = []
    added = set()
    for p in ctx.pages:
        if p in known or p in added:
            continue
        new_id = _next_page_id()
        try:
            p.set_default_timeout(10000)
        except Exception:
            pass

        # 为新标签页注册 filechooser / dialog / console / network（与 CoPaw 一致）
        choosers = _state["pending_file_choosers"].setdefault(new_id, [])
        dialogs = _state["pending_dialogs"].setdefault(new_id, [])
        _state["console_logs"].setdefault(new_id, [])
        _state["network_requests"].setdefault(new_id, [])

        def on_filechooser(chooser, pid=new_id):
            _state["pending_file_choosers"].setdefault(pid, choosers).append(chooser)

        def on_dialog(dialog, pid=new_id):
            _state["pending_dialogs"].setdefault(pid, dialogs).append(dialog)

        def on_console(msg, pid=new_id):
            _state["console_logs"].setdefault(pid, []).append({"level": getattr(msg, "type", "log"), "text": getattr(msg, "text", "")})

        def on_request(req, pid=new_id):
            _state["network_requests"].setdefault(pid, []).append({"url": req.url, "method": req.method, "resourceType": getattr(req, "resource_type", None)})

        def on_response(res, pid=new_id):
            for r in _state["network_requests"].get(pid, []):
                if r.get("url") == res.url and "status" not in r:
                    r["status"] = res.status
                    break

        try:
            p.on("filechooser", on_filechooser)
            p.on("dialog", on_dialog)
            p.on("console", on_console)
            p.on("request", on_request)
            p.on("response", on_response)
        except Exception:
            pass

        _state["pages"][new_id] = p
        _state["refs"][new_id] = {}
        _state["refs_frame"][new_id] = ""
        added.add(p)
        new_ids.append(new_id)
    if new_ids:
        _state["current_page_id"] = new_ids[0]
    return new_ids


def _parse_json_param(value, default=None):
    if not value or not isinstance(value, str):
        return default
    value = value.strip()
    if not value:
        return default
    try:
        return json.loads(value)
    except json.JSONDecodeError:
        if "," in value:
            return [x.strip() for x in value.split(",")]
        return default


async def action_click(page_id="default", selector=None, ref=None, frame_selector="", wait=0, double_click=False, button="left", modifiers_json=""):
    page = get_page(page_id)
    if not page:
        return {"ok": False, "error": "Page not found: " + page_id}
    try:
        if wait and wait > 0:
            await asyncio.sleep(wait / 1000.0)
        mods = _parse_json_param(modifiers_json, [])
        if not isinstance(mods, list):
            mods = []
        kwargs = {"button": button if button in ("left", "right", "middle") else "left"}
        if mods:
            kwargs["modifiers"] = [m for m in mods if m in ("Alt", "Control", "ControlOrMeta", "Meta", "Shift")]
        if ref:
            locator = get_locator_by_ref(page, page_id, ref, frame_selector)
            if locator is None and not selector:
                return {"ok": False, "error": "Unknown ref: " + ref}
            if locator is not None:
                if double_click:
                    await locator.dblclick(**kwargs)
                else:
                    await locator.click(**kwargs)
                new_tabs = await _collect_new_tabs_after_click()
                out = {"ok": True, "message": "Clicked ref " + ref}
                if new_tabs:
                    out["new_tabs"] = new_tabs
                return out
        sel = (selector or "").strip()
        if not sel:
            return {"ok": False, "error": "selector or ref required"}
        root = _get_root(page, page_id, frame_selector)
        locator = root.locator(sel).first if hasattr(root, "locator") else page.locator(sel).first
        if double_click:
            await locator.dblclick(**kwargs)
        else:
            await locator.click(**kwargs)
        new_tabs = await _collect_new_tabs_after_click()
        out = {"ok": True, "message": "Clicked " + sel}
        if new_tabs:
            out["new_tabs"] = new_tabs
        return out
    except Exception as e:
        return {"ok": False, "error": str(e)}


async def action_type(page_id="default", selector=None, ref=None, text="", submit=False, frame_selector="", slowly=False):
    page = get_page(page_id)
    if not page:
        return {"ok": False, "error": "Page not found: " + page_id}
    try:
        loc = None
        if ref:
            loc = get_locator_by_ref(page, page_id, ref, frame_selector)
        sel = (selector or "").strip() or (ref or "").strip()
        if loc is None:
            if not sel:
                return {"ok": False, "error": "selector or ref required"}
            root = _get_root(page, page_id, frame_selector)
            loc = root.locator(sel).first if hasattr(root, "locator") else page.locator(sel).first
        if slowly and text:
            await loc.press_sequentially(text)
        else:
            await loc.fill(text or "")
        try:
            await loc.dispatch_event("input")
            await loc.dispatch_event("change")
        except Exception:
            pass
        if submit:
            await loc.press("Enter")
        return {"ok": True, "message": "Typed into " + (ref or sel)}
    except Exception as e:
        return {"ok": False, "error": str(e)}


async def action_file_upload(page_id="default", paths_json=""):
    """
    使用最近一次触发的 FileChooser 上传本地文件列表。

    参数:
        page_id: 页面 ID（默认 default）
        paths_json: JSON 数组字符串，例如:
            "[\"C:\\\\path\\\\to\\\\file1.pdf\", \"C:\\\\path\\\\to\\\\file2.png\"]"
    """
    page = get_page(page_id)
    if not page:
        return {"ok": False, "error": "Page not found: " + page_id}

    raw = (paths_json or "").strip()
    if not raw:
        return {"ok": False, "error": "paths_json required (JSON array of file paths)"}

    try:
        paths = json.loads(raw)
    except Exception as e:
        return {
            "ok": False,
            "error": "Invalid paths_json: " + str(e),
        }

    if not isinstance(paths, list):
        return {"ok": False, "error": "paths_json must be a JSON array of strings"}

    choosers_map = _state.get("pending_file_choosers") or {}
    choosers = choosers_map.get(page_id) or []
    if not choosers:
        return {
            "ok": False,
            "error": "No pending file chooser. Click upload button on the page first, then call file_upload.",
        }

    chooser = choosers.pop(0)
    try:
        await chooser.set_files(paths)
        return {
            "ok": True,
            "message": "Uploaded {} file(s)".format(len(paths)),
            "paths": paths,
        }
    except Exception as e:
        return {"ok": False, "error": "File upload failed: " + str(e)}


async def action_eval(page_id="default", code=None):
    page = get_page(page_id)
    if not page:
        return {"ok": False, "error": "Page not found: " + page_id}
    if not (code and code.strip()):
        return {"ok": False, "error": "code required"}
    try:
        result = await page.evaluate("() => { return (" + code.strip() + "); }")
        return {"ok": True, "result": result}
    except Exception as e:
        return {"ok": False, "error": str(e)}


async def action_evaluate(page_id="default", code=None, ref="", frame_selector=""):
    """Run JS in page or on element (ref). When ref set, runs in element context."""
    code = (code or "").strip()
    if not code:
        return {"ok": False, "error": "code required for evaluate"}
    page = get_page(page_id)
    if not page:
        return {"ok": False, "error": "Page not found: " + page_id}
    try:
        if ref and ref.strip():
            locator = get_locator_by_ref(page, page_id, ref.strip(), frame_selector)
            if locator is None:
                return {"ok": False, "error": "Unknown ref: " + ref}
            result = await locator.evaluate(code)
        else:
            if code.startswith("(") or code.startswith("function"):
                result = await page.evaluate(code)
            else:
                result = await page.evaluate("() => { return (" + code + "); }")
        try:
            return {"ok": True, "result": result}
        except TypeError:
            return {"ok": True, "result": str(result)}
    except Exception as e:
        return {"ok": False, "error": str(e)}


async def action_press_key(page_id="default", key=None):
    page = get_page(page_id)
    if not page:
        return {"ok": False, "error": "Page not found: " + page_id}
    if not (key and key.strip()):
        return {"ok": False, "error": "key required"}
    try:
        await page.keyboard.press(key.strip())
        return {"ok": True, "message": "Pressed " + key}
    except Exception as e:
        return {"ok": False, "error": str(e)}


async def action_tabs(page_id="default", tab_action=None, index=-1):
    pages = _state["pages"]
    page_ids = list(pages.keys())
    tab_action = (tab_action or "").strip().lower()
    if tab_action == "list":
        return {"ok": True, "tabs": page_ids, "count": len(page_ids)}
    if tab_action == "new":
        ok = await ensure_browser(_state["headless"])
        if not ok:
            return {"ok": False, "error": "Browser not started"}
        page = await _state["context"].new_page()
        try:
            page.set_default_timeout(10000)
        except Exception:
            pass
        new_id = _next_page_id()
        choosers = _state["pending_file_choosers"].setdefault(new_id, [])
        dialogs = _state["pending_dialogs"].setdefault(new_id, [])
        _state["console_logs"].setdefault(new_id, [])
        _state["network_requests"].setdefault(new_id, [])

        def _on_filechooser(chooser):
            choosers.append(chooser)

        def _on_dialog(dialog):
            dialogs.append(dialog)

        def _on_console(msg):
            _state["console_logs"][new_id].append({"level": getattr(msg, "type", "log"), "text": getattr(msg, "text", "")})

        def _on_request(req):
            _state["network_requests"][new_id].append({"url": req.url, "method": req.method, "resourceType": getattr(req, "resource_type", None)})

        def _on_response(res):
            for r in _state["network_requests"][new_id]:
                if r.get("url") == res.url and "status" not in r:
                    r["status"] = res.status
                    break

        try:
            page.on("filechooser", _on_filechooser)
            page.on("dialog", _on_dialog)
            page.on("console", _on_console)
            page.on("request", _on_request)
            page.on("response", _on_response)
        except Exception:
            pass

        _state["pages"][new_id] = page
        _state["refs"][new_id] = {}
        _state["refs_frame"][new_id] = ""
        _state["current_page_id"] = new_id
        return {"ok": True, "page_id": new_id, "tabs": list(_state["pages"].keys())}
    if tab_action == "select":
        target_id = page_ids[index] if 0 <= index < len(page_ids) else page_id
        _state["current_page_id"] = target_id
        return {"ok": True, "message": "Use page_id={} for later actions".format(target_id), "page_id": target_id}
    if tab_action == "close":
        # CoPaw-compatible: close by index if valid, else close current page_id
        target_id = page_ids[index] if 0 <= index < len(page_ids) else page_id
        if target_id not in pages:
            return {"ok": False, "error": "Page not found: " + target_id}
        pid = target_id
        await pages[pid].close()
        del _state["pages"][pid]
        _state["refs"].pop(pid, None)
        _state.get("refs_frame", {}).pop(pid, None)
        _state.get("pending_file_choosers", {}).pop(pid, None)
        _state.get("pending_dialogs", {}).pop(pid, None)
        _state.get("console_logs", {}).pop(pid, None)
        _state.get("network_requests", {}).pop(pid, None)
        if _state.get("current_page_id") == pid:
            _state["current_page_id"] = list(_state["pages"].keys())[0] if _state["pages"] else None
        return {"ok": True, "message": "Closed " + pid}
    return {"ok": False, "error": "Unknown tab_action (use list, new, close, select)"}


async def action_handle_dialog(page_id="default", accept=True, prompt_text=""):
    page = get_page(page_id)
    if not page:
        return {"ok": False, "error": "Page not found: " + page_id}
    dialogs = _state.get("pending_dialogs", {}).get(page_id, [])
    if not dialogs:
        return {"ok": False, "error": "No pending dialog"}
    try:
        dialog = dialogs.pop(0)
        msg = ""
        try:
            # Playwright Dialog.message() may not exist in all environments; guard with hasattr
            msg = getattr(dialog, "message", "") or ""
        except Exception:
            msg = ""
        if accept:
            if prompt_text and hasattr(dialog, "accept"):
                await dialog.accept(prompt_text)
            else:
                await dialog.accept()
        else:
            await dialog.dismiss()
        result = {"ok": True, "message": "Dialog handled"}
        if msg:
            # Expose dialog text so callers (and LLM) can understand what was shown
            result["dialog_text"] = msg
        return result
    except Exception as e:
        return {"ok": False, "error": str(e)}


async def action_close(page_id="default"):
    page = get_page(page_id)
    if not page:
        return {"ok": False, "error": "Page not found: " + page_id}
    try:
        await page.close()
        _state["pages"].pop(page_id, None)
        _state["refs"].pop(page_id, None)
        _state.get("refs_frame", {}).pop(page_id, None)
        _state.get("pending_file_choosers", {}).pop(page_id, None)
        _state.get("pending_dialogs", {}).pop(page_id, None)
        _state.get("console_logs", {}).pop(page_id, None)
        _state.get("network_requests", {}).pop(page_id, None)
        return {"ok": True, "message": "Closed page " + page_id}
    except Exception as e:
        return {"ok": False, "error": str(e)}


async def action_navigate_back(page_id="default"):
    page = get_page(page_id)
    if not page:
        return {"ok": False, "error": "Page not found: " + page_id}
    try:
        await page.go_back()
        return {"ok": True, "message": "Navigated back", "url": page.url}
    except Exception as e:
        return {"ok": False, "error": str(e)}


async def action_resize(page_id="default", width=0, height=0):
    if width <= 0 or height <= 0:
        return {"ok": False, "error": "width and height must be positive"}
    page = get_page(page_id)
    if not page:
        return {"ok": False, "error": "Page not found: " + page_id}
    try:
        await page.set_viewport_size({"width": width, "height": height})
        return {"ok": True, "message": "Resized to {}x{}".format(width, height)}
    except Exception as e:
        return {"ok": False, "error": str(e)}


async def action_pdf(page_id="default", path=None):
    page = get_page(page_id)
    if not page:
        return {"ok": False, "error": "Page not found: " + page_id}
    path = (path or "").strip() or "/tmp/page.pdf"
    try:
        await page.pdf(path=path)
        return {"ok": True, "message": "PDF saved", "path": path}
    except Exception as e:
        return {"ok": False, "error": str(e)}


async def action_wait_for(page_id="default", wait_time=0, text=None, text_gone=None):
    page = get_page(page_id)
    if not page:
        return {"ok": False, "error": "Page not found: " + page_id}
    if wait_time and wait_time > 0:
        await asyncio.sleep(wait_time)
    if text and text.strip():
        try:
            await page.get_by_text(text.strip()).wait_for(state="visible", timeout=30000)
        except Exception as e:
            return {"ok": False, "error": str(e)}
    if text_gone and text_gone.strip():
        try:
            await page.get_by_text(text_gone.strip()).wait_for(state="hidden", timeout=30000)
        except Exception as e:
            return {"ok": False, "error": str(e)}
    return {"ok": True, "message": "Wait completed"}


async def action_hover(page_id="default", selector=None, ref=None, frame_selector=""):
    page = get_page(page_id)
    if not page:
        return {"ok": False, "error": "Page not found: " + page_id}
    if not ref and not (selector and selector.strip()):
        return {"ok": False, "error": "hover requires ref or selector"}
    try:
        if ref:
            locator = get_locator_by_ref(page, page_id, ref, frame_selector)
            if locator is None:
                return {"ok": False, "error": "Unknown ref: " + ref}
        else:
            root = _get_root(page, page_id, frame_selector)
            locator = root.locator(selector.strip()).first if hasattr(root, "locator") else page.locator(selector.strip()).first
        await locator.hover()
        return {"ok": True, "message": "Hovered " + (ref or selector)}
    except Exception as e:
        return {"ok": False, "error": str(e)}


async def action_select_option(page_id="default", ref="", values_json="", frame_selector=""):
    values = _parse_json_param(values_json, [])
    if not isinstance(values, list):
        values = [values] if values is not None else []
    if not (ref and ref.strip()):
        return {"ok": False, "error": "ref required for select_option"}
    if not values:
        return {"ok": False, "error": "values required (JSON array or comma-separated)"}
    page = get_page(page_id)
    if not page:
        return {"ok": False, "error": "Page not found: " + page_id}
    try:
        locator = get_locator_by_ref(page, page_id, ref.strip(), frame_selector)
        if locator is None:
            return {"ok": False, "error": "Unknown ref: " + ref}
        await locator.select_option(value=values)
        return {"ok": True, "message": "Selected " + str(values)}
    except Exception as e:
        return {"ok": False, "error": str(e)}


async def action_console_messages(page_id="default", level="info", filename=""):
    page = get_page(page_id)
    if not page:
        return {"ok": False, "error": "Page not found: " + page_id}
    logs = _state.get("console_logs", {}).get(page_id, [])
    level_lower = (level or "info").strip().lower()
    allowed = {"verbose": ["verbose", "debug", "info", "warning", "error"], "debug": ["debug", "info", "warning", "error"], "info": ["info", "warning", "error"], "warning": ["warning", "error"], "error": ["error"]}
    include_levels = allowed.get(level_lower, ["info", "warning", "error"])
    filtered = [m for m in logs if (m.get("level") or "info").lower() in include_levels]
    if filename and filename.strip():
        try:
            with open(filename.strip(), "w", encoding="utf-8") as f:
                f.write(json.dumps(filtered, ensure_ascii=False, indent=2))
        except Exception:
            pass
    return {"ok": True, "messages": filtered, "count": len(filtered)}


async def action_network_requests(page_id="default", include_static=False, filename=""):
    page = get_page(page_id)
    if not page:
        return {"ok": False, "error": "Page not found: " + page_id}
    requests = _state.get("network_requests", {}).get(page_id, [])
    if not include_static:
        requests = [r for r in requests if r.get("resourceType") not in ("stylesheet", "image", "font", "media")]
    if filename and filename.strip():
        try:
            with open(filename.strip(), "w", encoding="utf-8") as f:
                f.write(json.dumps(requests, ensure_ascii=False, indent=2))
        except Exception:
            pass
    return {"ok": True, "requests": requests, "count": len(requests)}


async def action_fill_form(page_id="default", fields_json=""):
    """Batch fill form fields by ref. fields_json: list of {ref, type?, value?}. type: textbox|checkbox|radio|combobox|slider."""
    page = get_page(page_id)
    if not page:
        return {"ok": False, "error": "Page not found: " + page_id}
    fields = _parse_json_param(fields_json, [])
    if not isinstance(fields, list) or not fields:
        return {"ok": False, "error": "fields required (JSON array of {ref, type?, value?})"}
    refs = get_refs(page_id)
    frame = _state.get("refs_frame", {}).get(page_id, "")
    filled = 0
    try:
        for f in fields:
            ref = (f.get("ref") or "").strip()
            if not ref or ref not in refs:
                continue
            locator = get_locator_by_ref(page, page_id, ref, frame)
            if locator is None:
                continue
            field_type = (f.get("type") or "textbox").lower()
            value = f.get("value")
            if field_type == "checkbox":
                if isinstance(value, str):
                    value = value.strip().lower() in ("true", "1", "yes")
                await locator.set_checked(bool(value))
            elif field_type == "radio":
                await locator.set_checked(True)
            elif field_type == "combobox":
                await locator.select_option(label=value if isinstance(value, str) else None, value=value)
            elif field_type == "slider":
                await locator.fill(str(value))
            else:
                await locator.fill(str(value) if value is not None else "")
            filled += 1
        return {"ok": True, "message": "Filled {} field(s)".format(filled), "filled": filled}
    except Exception as e:
        return {"ok": False, "error": "Fill form failed: " + str(e)}


async def action_drag(page_id="default", start_ref="", end_ref="", start_selector="", end_selector="", frame_selector=""):
    """Drag from start to end. Use (start_ref, end_ref) or (start_selector, end_selector)."""
    start_ref = (start_ref or "").strip()
    end_ref = (end_ref or "").strip()
    start_selector = (start_selector or "").strip()
    end_selector = (end_selector or "").strip()
    use_refs = bool(start_ref and end_ref)
    use_selectors = bool(start_selector and end_selector)
    if not use_refs and not use_selectors:
        return {"ok": False, "error": "drag needs (start_ref, end_ref) or (start_selector, end_selector)"}
    page = get_page(page_id)
    if not page:
        return {"ok": False, "error": "Page not found: " + page_id}
    try:
        if use_refs:
            start_locator = get_locator_by_ref(page, page_id, start_ref, frame_selector)
            end_locator = get_locator_by_ref(page, page_id, end_ref, frame_selector)
            if start_locator is None or end_locator is None:
                return {"ok": False, "error": "Unknown ref for drag"}
        else:
            root = _get_root(page, page_id, frame_selector)
            start_locator = root.locator(start_selector).first if hasattr(root, "locator") else page.locator(start_selector).first
            end_locator = root.locator(end_selector).first if hasattr(root, "locator") else page.locator(end_selector).first
        await start_locator.drag_to(end_locator)
        return {"ok": True, "message": "Drag completed"}
    except Exception as e:
        return {"ok": False, "error": "Drag failed: " + str(e)}


async def action_run_code(page_id="default", code=""):
    """Run JS in page. If code starts with ( or function, evaluate as-is; else wrap as () => return (code)."""
    code = (code or "").strip()
    if not code:
        return {"ok": False, "error": "code required for run_code"}
    page = get_page(page_id)
    if not page:
        return {"ok": False, "error": "Page not found: " + page_id}
    try:
        if code.startswith("(") or code.startswith("function"):
            result = await page.evaluate(code)
        else:
            result = await page.evaluate("() => { return (" + code + "); }")
        return {"ok": True, "result": result}
    except Exception as e:
        return {"ok": False, "error": "Run code failed: " + str(e)}


async def action_install():
    """Install Playwright browsers. In sandbox usually pre-installed; return message."""
    try:
        import subprocess
        proc = subprocess.run(
            [sys.executable, "-m", "playwright", "install", "chromium"],
            capture_output=True,
            text=True,
            timeout=300,
        )
        if proc.returncode == 0:
            return {"ok": True, "message": "Playwright chromium installed"}
        return {"ok": False, "error": proc.stderr or "install failed"}
    except Exception as e:
        return {"ok": True, "message": "Sandbox may use pre-installed browser: " + str(e)}


async def dispatch(params):
    action = (params.get("action") or "").strip().lower()
    if not action:
        return {"ok": False, "error": "action required"}

    page_id = (params.get("page_id") or "default").strip() or "default"
    url = (params.get("url") or "").strip()
    selector = (params.get("selector") or "").strip()
    ref = (params.get("ref") or "").strip()
    text = params.get("text") or ""
    code = (params.get("code") or "").strip()
    path = (params.get("path") or "").strip()
    full_page = bool(params.get("full_page"))
    width = int(params.get("width") or 0)
    height = int(params.get("height") or 0)
    key = (params.get("key") or "").strip()
    tab_action = (params.get("tab_action") or "").strip()
    index = int(params.get("index") or -1)
    wait_time = float(params.get("wait_time") or 0)
    headed = bool(params.get("headed"))
    submit = bool(params.get("submit"))
    paths_json = (params.get("paths_json") or "").strip()
    frame_selector = (params.get("frame_selector") or "").strip()
    wait = int(params.get("wait") or 0)
    double_click = bool(params.get("double_click"))
    button = (params.get("button") or "left").strip()
    modifiers_json = (params.get("modifiers_json") or "").strip()
    slowly = bool(params.get("slowly"))
    snapshot_filename = (params.get("snapshot_filename") or params.get("filename") or "").strip()
    accept = bool(params.get("accept") if params.get("accept") is not None else True)
    prompt_text = (params.get("prompt_text") or "").strip()
    text_gone = (params.get("text_gone") or "").strip()
    level = (params.get("level") or "info").strip()
    filename = (params.get("filename") or "").strip()
    include_static = bool(params.get("include_static"))
    values_json = (params.get("values_json") or "").strip()
    screenshot_type = (params.get("screenshot_type") or "png").strip()
    fields_json = (params.get("fields_json") or "").strip()
    start_ref = (params.get("start_ref") or "").strip()
    end_ref = (params.get("end_ref") or "").strip()
    start_selector = (params.get("start_selector") or "").strip()
    end_selector = (params.get("end_selector") or "").strip()

    # default page_id 解析为 current_page_id（与 CoPaw 一致）
    page_id = _resolve_page_id(page_id)

    handlers = {
        "start": lambda: action_start(headed=headed),
        "get_connection_mode": action_get_connection_mode,
        "stop": action_stop,
        "restart_server": action_restart_server,
        "open": lambda: action_open(url, page_id),
        "navigate": lambda: action_navigate(url, page_id),
        "navigate_back": lambda: action_navigate_back(page_id),
        "snapshot": lambda: action_snapshot(page_id, frame_selector, snapshot_filename),
        "screenshot": lambda: action_screenshot(page_id, path or None, full_page, ref, frame_selector, screenshot_type),
        "click": lambda: action_click(page_id, selector or ref or None, ref or None, frame_selector, wait, double_click, button, modifiers_json),
        "type": lambda: action_type(page_id, selector or ref or None, ref or None, text, submit, frame_selector, slowly),
        "file_upload": lambda: action_file_upload(page_id, paths_json),
        "handle_dialog": lambda: action_handle_dialog(page_id, accept, prompt_text),
        "hover": lambda: action_hover(page_id, selector or ref or None, ref or None, frame_selector),
        "select_option": lambda: action_select_option(page_id, ref, values_json, frame_selector),
        "console_messages": lambda: action_console_messages(page_id, level, filename or path),
        "network_requests": lambda: action_network_requests(page_id, include_static, filename or path),
        "fill_form": lambda: action_fill_form(page_id, fields_json),
        "drag": lambda: action_drag(page_id, start_ref, end_ref, start_selector, end_selector, frame_selector),
        "run_code": lambda: action_run_code(page_id, code),
        "install": action_install,
        "eval": lambda: action_eval(page_id, code),
        "evaluate": lambda: action_evaluate(page_id, code, ref or "", frame_selector),
        "press_key": lambda: action_press_key(page_id, key),
        "tabs": lambda: action_tabs(page_id, tab_action, index),
        "close": lambda: action_close(page_id),
        "resize": lambda: action_resize(page_id, width, height),
        "pdf": lambda: action_pdf(page_id, path or None),
        "wait_for": lambda: action_wait_for(page_id, wait_time, text, text_gone),
    }
    if action not in handlers:
        return {"ok": False, "error": "Unknown action: " + action}
    return await handlers[action]()


def run_server(user_id):
    sock_path = get_socket_path(user_id)
    pid_path = get_pid_path(user_id)
    if os.path.exists(sock_path):
        try:
            os.unlink(sock_path)
        except OSError:
            pass

    async def handle(reader, writer):
        try:
            data = await reader.readuntil(b"\n")
            params = json.loads(data.decode("utf-8").strip())
            result = await dispatch(params)
            out = json.dumps(result, ensure_ascii=False) + "\n"
            writer.write(out.encode("utf-8"))
            await writer.drain()
        except Exception as e:
            writer.write((json.dumps({"ok": False, "error": str(e)}, ensure_ascii=False) + "\n").encode("utf-8"))
            await writer.drain()
        finally:
            writer.close()
            try:
                await writer.wait_closed()
            except Exception:
                pass

    async def main():
        server = await asyncio.start_unix_server(handle, path=sock_path)
        async with server:
            await server.serve_forever()

    asyncio.run(main())


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--user-id", required=True, help="User ID for sandbox session")
    parser.add_argument("--params-base64", required=False, help="Base64-encoded JSON params (not needed when running as server)")
    args = parser.parse_args()
    user_id = args.user_id

    if os.environ.get("BOTE_BROWSER_SERVER") == "1":
        run_server(user_id)
        return
    if not args.params_base64:
        print(json.dumps({"ok": False, "error": "params-base64 required"}, ensure_ascii=False))
        sys.exit(1)
    try:
        params = json.loads(base64.b64decode(args.params_base64).decode("utf-8"))
    except Exception as e:
        print(json.dumps({"ok": False, "error": "Invalid params: " + str(e)}, ensure_ascii=False))
        sys.exit(1)
    run_client(user_id, params)


if __name__ == "__main__":
    main()
