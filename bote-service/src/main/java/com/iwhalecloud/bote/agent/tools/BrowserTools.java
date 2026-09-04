package com.iwhalecloud.bote.agent.tools;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.iwhalecloud.bote.agent.annotation.Tool;
import com.iwhalecloud.bote.agent.annotation.ToolRequest;
import com.iwhalecloud.bote.agent.tool.context.ToolContext;
import com.iwhalecloud.bote.agent.tool.exception.ToolExecutionException;
import com.iwhalecloud.bote.cache.GeneraAgentCache;
import com.iwhalecloud.bote.common.enums.SandboxMode;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.sandbox.config.SandboxEngineProperties;
import com.iwhalecloud.bote.sandbox.dto.SandboxFileReadResult;
import com.iwhalecloud.bote.sandbox.dto.SandboxRunRequest;
import com.iwhalecloud.bote.sandbox.dto.SandboxRunResult;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 浏览器自动化工具（参考 CoPaw browser_control）。
 *
 * @author zhaoxu
 */
public final class BrowserTools {
  /** 脚本文件名，写入沙箱时为 workDir/SCRIPT_NAME */
  private static final String SCRIPT_NAME = "browser_runner.py";
  /** workDir 未设置时使用的默认脚本路径 */
  public static final String DEFAULT_BROWSER_RUNNER_PATH = "/opt/bote/browser_runner.py";

  private BrowserTools() {
  }

  @Tool(
    name = "browser_use",
    description = "Control browser in sandbox (Playwright). Flow: start, open(url), snapshot (frame_selector for iframe), click/type/hover/select_option/fill_form/drag with ref or selector. tabs: list, new, close, select (index); default page_id uses current tab. open/tabs new return page_id (page_1, page_2...). For file upload: click upload then file_upload+pathsJson. For native browser dialogs (alert/confirm/prompt) that block further actions or cause screenshot/snapshot timeouts, use handle_dialog to accept/dismiss them. eval=page-level JS; evaluate=JS with optional ref+frame_selector (run on element). Actions: start, stop, open, navigate, navigate_back, snapshot, screenshot, take_screenshot, click, type, file_upload, handle_dialog, hover, select_option, fill_form, drag, run_code, install, console_messages, network_requests, eval, evaluate, resize, press_key, tabs, wait_for, pdf, close."
  )
  public static String browserUse(@ToolRequest BrowserUseRequest request, ToolContext toolContext) {
    if (toolContext.sandboxMode() == SandboxMode.CLIENT) {
      return toolContext.webSocketChatContext().invokeTool("browser_use", request);
    }
    Assert.isTrue(toolContext.sandboxMode() == SandboxMode.REMOTE, "Error: Browser tools are only available when sandbox is enabled. Enable sandbox for this session to use browser_use.");
    try {
      ensureBrowserRunnerInSandbox(toolContext);
      String out = executeBrowserCommand(toolContext, request);
      return out.isEmpty() ? "ok" : out;
    }
    catch (ToolExecutionException e) {
      throw e;
    }
    catch (Exception e) {
      throw new ToolExecutionException("Error: " + ExpUtil.getMsg(e), e);
    }
  }

  @Tool(
    name = "get_vnc_url",
    description = "Get VNC URL for current sandbox browser session. This is available only when browser is connected in cdp mode."
  )
  public static String getVncUrl(ToolContext toolContext) {
    Assert.isTrue(toolContext.sandboxMode() == SandboxMode.REMOTE, "Error: Browser tools are only available when sandbox is enabled. Enable sandbox for this session to use get_vnc_url.");
    try {
      ensureBrowserRunnerInSandbox(toolContext);
      return resolveVncUrl(toolContext);
    }
    catch (ToolExecutionException e) {
      throw e;
    }
    catch (Exception e) {
      throw new ToolExecutionException("Error: " + ExpUtil.getMsg(e), e);
    }
  }

  private static String buildCommand(ToolContext toolContext, BrowserUseRequest request) {
    String paramsJson = JsonUtil.toJsonString(request);
    String paramsBase64 = Base64.getEncoder().encodeToString(paramsJson.getBytes(StandardCharsets.UTF_8));
    return String.format("python3 %s --user-id %s --params-base64 %s", DEFAULT_BROWSER_RUNNER_PATH, toolContext.userId(), paramsBase64);
  }

  private static void appendErrorMessage(StringBuilder out, SandboxRunResult result) {
    if (result.isSuccess() || result.getErrorMessage() == null) {
      return;
    }
    if (!out.isEmpty()) {
      out.append("\n");
    }
    out.append("Error: ").append(result.getErrorMessage());
  }

  private static String executeBrowserCommand(ToolContext toolContext, BrowserUseRequest request) {
    String command = buildCommand(toolContext, request);
    SandboxRunRequest sandboxRunRequest = SandboxRunRequest.builder().command(command).build();
    SandboxRunResult result = toolContext.sandboxClient().execute(sandboxRunRequest);
    StringBuilder out = new StringBuilder();
    String stdout = StringUtils.trimToNull(result.getStdout());
    String stderr = StringUtils.trimToNull(result.getStderr());
    if (stdout != null) {
      out.append(stdout);
    }
    if (stderr != null) {
      if (!out.isEmpty() && out.charAt(out.length() - 1) != '\n') {
        out.append("\n");
      }
      out.append("STDERR:\n").append(stderr);
    }
    appendErrorMessage(out, result);
    if (!result.isSuccess()) {
      throw new ToolExecutionException(out.isEmpty() ? "Error: browser command failed" : out.toString());
    }
    return out.toString();
  }

  private static String resolveVncUrl(ToolContext toolContext) {
    BrowserUseRequest verifyRequest = new BrowserUseRequest();
    verifyRequest.setAction("get_connection_mode");
    String out = executeBrowserCommand(toolContext, verifyRequest);
    JsonNode json = parseFirstJson(out);
    if (json == null) {
      throw new ToolExecutionException("Error: failed to parse browser status");
    }
    String mode = json.path("connection_mode").asText("");
    if (!"cdp".equalsIgnoreCase(mode)) {
      throw new ToolExecutionException("Error: VNC URL is available only in cdp mode. Current mode: " + (mode.isBlank() ? "unknown" : mode));
    }
    String vncUrl = buildVncUrl(toolContext);
    return JsonUtil.toJsonString(Map.of("ok", true, "connection_mode", "cdp", "vnc_url", vncUrl));
  }

  @Nullable
  private static JsonNode parseFirstJson(String out) {
    if (out.isBlank()) {
      return null;
    }
    String[] lines = out.split("\\r?\\n");
    for (String line : lines) {
      String s = line == null ? "" : line.trim();
      if (!s.startsWith("{")) {
        continue;
      }
      try {
        return JsonUtil.readTree(s);
      }
      catch (Exception ignored) {
        // ignore
      }
    }
    try {
      return JsonUtil.readTree(out.trim());
    }
    catch (Exception ignored) {
      return null;
    }
  }

  private static String buildVncUrl(ToolContext toolContext) {
    GeneraAgentCache cache = SpringUtil.getBean(GeneraAgentCache.class);
    Map<String, String> envVariables = cache.getEnvVariables(toolContext.tenantId(), toolContext.botId(), toolContext.userId());
    String baseUrl = envVariables.get("BOTE_VNC_BASE_URL");
    if (StringUtils.isEmpty(baseUrl)) {
      baseUrl = SpringUtil.getBean(SandboxEngineProperties.class).getConnection().getBaseUrl();
    }
    String vncPort = StringUtils.defaultIfEmpty(envVariables.get("BOTE_VNC_PORT"), "8080");
    String password = StringUtils.defaultIfEmpty(envVariables.get("BOTE_VNC_PASSWORD"), "ztesoft");
    String sandboxId = toolContext.sandboxClient().getSandboxId();
    String url = String.format("%s/proxy/%s/%s/novnc/vnc.html", baseUrl, sandboxId, vncPort);
    return UriComponentsBuilder.fromUriString(url)
      .queryParam("autoconnect", "1")
      .queryParam("path", "websockify")
      .queryParam("password", password)
      .encode()
      .build()
      .toUriString();
  }

  /**
   * 仅当沙箱中不存在脚本时，将 classpath 下的 browser_runner.py 写入沙箱指定路径（只上传一次）。
   */
  private static void ensureBrowserRunnerInSandbox(ToolContext toolContext) {
    SandboxFileReadResult readResult = toolContext.sandboxClient().readFile(DEFAULT_BROWSER_RUNNER_PATH);
    if (readResult.isSuccess()) {
      return;
    }
    ClassPathResource resource = new ClassPathResource("browser/" + SCRIPT_NAME);
    if (!resource.exists()) {
      return;
    }
    try {
      String content = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
      toolContext.sandboxClient().writeFile(DEFAULT_BROWSER_RUNNER_PATH, content);
    }
    catch (Exception e) {
      throw new BssException("Failed to write browser runner script to sandbox: " + e.getMessage(), e);
    }
  }

  /**
   * 浏览器工具请求对象
   */
  @Getter
  @Setter
  @ToString
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonNaming(SnakeCaseStrategy.class)
  public static class BrowserUseRequest {
    @Schema(description = "Action: start, stop, restart_server, open, navigate, navigate_back, snapshot, screenshot, take_screenshot, click, type, file_upload, handle_dialog, hover, select_option, fill_form, drag, run_code, install, console_messages, network_requests, eval, evaluate, resize, press_key, tabs, wait_for, pdf, close", requiredMode = RequiredMode.REQUIRED)
    private String action;
    @Schema(description = "URL to open; required for action open or navigate")
    private String url;
    @Schema(description = "Page/tab id, default 'default'")
    private String pageId;
    @Schema(description = "CSS selector; prefer ref from snapshot for click/type/hover")
    private String selector;
    @Schema(description = "Text to type; required for action=type")
    private String text;
    @Schema(description = "JavaScript code. For eval: page-level expression. For evaluate: expression or function; when ref is set, run on element (e.g. element => element.value)")
    private String code;
    @Schema(description = "Path for screenshot/PDF or filename for console_messages/network_requests")
    private String path;
    @Schema(description = "Wait milliseconds after click")
    private Integer wait;
    @Schema(description = "Full page screenshot")
    private Boolean fullPage;
    @Schema(description = "Viewport width for action=resize")
    private Integer width;
    @Schema(description = "Viewport height for action=resize")
    private Integer height;
    @Schema(description = "Element ref from snapshot (e.g. e13). Used by click, type, hover, select_option, screenshot, evaluate (run JS on element when action=evaluate).")
    private String ref;
    @Schema(description = "Key for action=press_key, e.g. Enter, Control+a")
    private String key;
    @Schema(description = "For action=tabs: list, new, close, select")
    private String tabAction;
    @Schema(description = "Tab index for action=tabs close/select")
    private Integer index;
    @Schema(description = "Seconds to wait for action=wait_for")
    private Double waitTime;
    @Schema(description = "Launch visible browser when true with action=start")
    private Boolean headed;
    @Schema(description = "If true with action=type, press Enter after typing")
    private Boolean submit;
    @Schema(description = "For action=file_upload: JSON array of file paths in sandbox. Click upload button first.")
    private String pathsJson;
    @Schema(description = "Iframe selector for snapshot/click/type/hover/select_option/screenshot/evaluate, e.g. 'iframe#main' or 'iframe >> nth=2'")
    private String frameSelector;
    @Schema(description = "Double-click when true with action=click")
    private Boolean doubleClick;
    @Schema(description = "Mouse button for click: left, right, middle")
    private String button;
    @Schema(description = "Modifier keys for click, JSON array e.g. [\"Shift\",\"Control\"]")
    private String modifiersJson;
    @Schema(description = "Type character-by-character when true with action=type")
    private Boolean slowly;
    @Schema(description = "Save snapshot to this file path for action=snapshot")
    private String snapshotFilename;
    @Schema(description = "For action=handle_dialog: whether to accept (true) or dismiss/cancel (false) the current native browser dialog (alert/confirm/prompt). When screenshot/snapshot keeps timing out, the model should call handle_dialog with accept=true to close the blocking dialog before continuing.")
    private Boolean accept;
    @Schema(description = "For action=handle_dialog: text to send to the dialog when it is a prompt (ignored for simple alert/confirm). Leave empty for normal OK/Cancel dialogs.")
    private String promptText;
    @Schema(description = "Wait until this text disappears for action=wait_for")
    private String textGone;
    @Schema(description = "Console log level filter for action=console_messages: info, warning, error")
    private String level;
    @Schema(description = "Filename to save output for console_messages or network_requests")
    private String filename;
    @Schema(description = "Include static resources (css, image, font) for action=network_requests")
    private Boolean includeStatic;
    @Schema(description = "For action=select_option: JSON array of option values to select")
    private String valuesJson;
    @Schema(description = "Screenshot format: png or jpeg")
    private String screenshotType;
    @Schema(description = "For action=fill_form: JSON array of {ref, type?, value?}, type=textbox|checkbox|radio|combobox|slider")
    private String fieldsJson;
    @Schema(description = "Drag start element ref for action=drag")
    private String startRef;
    @Schema(description = "Drag end element ref for action=drag")
    private String endRef;
    @Schema(description = "Drag start CSS selector for action=drag")
    private String startSelector;
    @Schema(description = "Drag end CSS selector for action=drag")
    private String endSelector;
  }
}
