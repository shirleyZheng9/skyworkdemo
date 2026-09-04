package com.iwhalecloud.bote.service.orchestration.runner.step;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.common.util.ParamConverterUtil;
import com.iwhalecloud.bote.common.util.PythonUtil;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.orchestration.context.SceneOrchestrationContext;
import com.iwhalecloud.bote.dto.orchestration.file.AbstractFile;
import com.iwhalecloud.bote.dto.orchestration.file.DataUrlFile;
import com.iwhalecloud.bote.dto.orchestration.file.FileServerFile;
import com.iwhalecloud.bote.dto.orchestration.file.UrlFile;
import com.iwhalecloud.bote.dto.orchestration.log.OrchestrationStepRunLog;
import com.iwhalecloud.bote.dto.orchestration.step.PlaywrightStep;
import com.iwhalecloud.bote.service.orchestration.helper.AgentPoolClient;
import com.iwhalecloud.bote.service.orchestration.runner.AbstractStepRunner;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreService;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.io.File;
import java.io.StringWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * PlayWright 自动化步骤执行器
 *
 * @author bianjp
 * @since 2026-01-14
 */
@SuppressWarnings("PMD.GuardLogStatement")
public class PlaywrightStepRunner extends AbstractStepRunner<PlaywrightStep> {
  private final AgentPoolClient agentPoolClient = SpringUtil.getBean(AgentPoolClient.class);
  private final IFileStoreService fileStoreService = SpringUtil.getBean(IFileStoreService.class);

  @Override
  protected void doRun(SceneOrchestrationContext context, PlaywrightStep step) {
    Assert.hasLength(step.getScriptContent(), "脚本内容不能为空");
    String userCode = resolveUserCode(context);
    String chatId = resolveChatId(context, step);
    Map<String, Object> parameters = buildRequestParametersToMap(step.getParameters());

    Optional<OrchestrationStepRunLog> logOptional = context.getLastStepRunLogOptional();
    logOptional.ifPresent(log -> {
      Map<String, Object> input = new LinkedHashMap<>();
      input.put("userCode", userCode);
      input.put("chatId", chatId);
      input.put("parameters", parameters);
      log.setInput(input);
    });

    // 创建沙箱
    long startTime = System.currentTimeMillis();
    String cdpUrl = agentPoolClient.getCdpUrl(userCode, chatId);
    long spentTime = System.currentTimeMillis() - startTime;
    logOptional.ifPresent(log -> {
      log.addLog("创建沙箱成功，耗时: %sms", spentTime);
      log.addLog("cdp url: %s", cdpUrl);
    });

    // 发送开始事件
    sendPlaywrightStartEvent(context, step, userCode, chatId, logOptional);

    // 创建临时目录
    File tmpDir;
    try {
      tmpDir = Files.createTempDirectory("bote-playwright-").toFile();
      logger.trace("Created temporary directory: {}", tmpDir.getAbsolutePath());
      // 临时目录中存储了上传的文件、下载的文件，需要在工作流执行结束后再清除
      context.addCleaner(() -> FileUtils.deleteQuietly(tmpDir));
    }
    catch (Exception e) {
      throw new BssException("创建临时目录失败: " + ExpUtil.getMsg(e), e);
    }

    // 上传文件
    List<String> uploadedFiles = uploadFiles(tmpDir, step);
    logOptional.ifPresent(log -> log.addLog("files: " + uploadedFiles));

    try {
      // 执行脚本
      Map<String, Object> result = executeScript(step, cdpUrl, parameters, uploadedFiles, tmpDir, logOptional.orElse(null));
      // 转换出参
      Map<String, Object> convertedResult = convertScriptOutput(step, result, tmpDir);
      context.setStepOutput(step, convertedResult, logOptional.orElse(null));
    }
    catch (Exception e) {
      // 失败时可以提前删除临时目录
      FileUtils.deleteQuietly(tmpDir);
      throw e;
    }

    // 发送结束事件
    sendPlaywrightStopEvent(context, step);
    // 自动释放沙箱
    if (!Boolean.FALSE.equals(step.getAutoRelease())) {
      agentPoolClient.removeSandboxQuietly(userCode, chatId);
    }
  }

  /**
   * 解析用户编码
   *
   * <p>Agent Pool 的用户编码要求是合法的 MinIO bucket 名称: 长度 3~63，只能包含小写字母、数字、"."、"-"</p>
   */
  private String resolveUserCode(SceneOrchestrationContext context) {
    Long userId = SessionUtil.getOptionalUserId();
    // 优先使用用户 ID
    if (userId != null) {
      return "bote-user-" + userId;
    }
    // 其次使用租户 ID
    return "bote-tenant-" + context.getTenantId();
  }

  /**
   * 解析会话 ID
   */
  private String resolveChatId(SceneOrchestrationContext context, PlaywrightStep step) {
    String chatId;
    // 自定义会话 ID
    if (Boolean.TRUE.equals(step.getUseCustomChatId())) {
      chatId = resolveTemplate(step.getChatId());
      Assert.hasLength(chatId, "会话 ID 不能为空");
      Assert.isTrue(chatId.length() <= 128, "会话 ID 长度不能超过 128 个字符");
    }
    else {
      // 自动管理时使用上下文 ID
      chatId = context.getRequest().getContextId();
      Assert.hasLength(chatId, "上下文 ID 不能为空");
    }
    return chatId;
  }

  /**
   * 支持脚本
   */
  @Nullable
  private Map<String, Object> executeScript(PlaywrightStep step, String cdpUrl, Map<String, Object> parameters,
                                            List<String> uploadedFiles, File tmpDir, @Nullable OrchestrationStepRunLog log) {
    StringWriter stdoutWriter = new StringWriter();
    try {
      Map<String, Object> arguments = new LinkedHashMap<>();
      arguments.put("cdp_url", cdpUrl);
      arguments.put("params", parameters);
      arguments.put("files", uploadedFiles);
      return PythonUtil.invokePlaywrightScript(tmpDir, step.getScriptContent(), stdoutWriter, arguments);
    }
    catch (BssException e) {
      throw e;
    }
    catch (Exception e) {
      throw new BssException("浏览器自动化脚本执行失败: " + ExpUtil.getMsg(e), e);
    }
    finally {
      // 将脚本的标准输出记录到步骤日志中，方便排查问题
      if (log != null && !stdoutWriter.getBuffer().isEmpty()) {
        log.addLog("stdout: %s", stdoutWriter.toString());
      }
    }
  }

  /**
   * 转换脚本出参
   */
  private Map<String, Object> convertScriptOutput(PlaywrightStep step, @Nullable Map<String, Object> result, File tmpDir) {
    if (MapUtils.isEmpty(result)) {
      return Map.of();
    }
    // 未配置出参结构时不转换
    ParameterSpec outData = step.getOutData();
    if (outData == null || !outData.hasChildren()) {
      return result;
    }
    Map<String, Object> convertedResult = new LinkedHashMap<>();
    for (ParameterSpec child : outData.getChildren()) {
      Object value = result.get(child.getName());
      if (value == null) {
        continue;
      }
      Object convertedValue = convertScriptOutputItem(value, tmpDir, child);
      if (convertedValue != null) {
        convertedResult.put(child.getName(), convertedValue);
      }
    }
    return convertedResult;
  }

  /**
   * 转换脚本出参中的一项
   */
  @Nullable
  private Object convertScriptOutputItem(Object value, File tmpDir, ParameterSpec child) {
    // 特殊处理文件，值当作本地文件路径
    if (child.getType() == AttrDataType.FILE) {
      Assert.isTrue(value instanceof String, () -> "脚本出参中的 " + child.getName() + " 必须是字符串类型，实际是: " + value.getClass().getName());
      File file = tmpDir.toPath().resolve((String) value).toFile();
      Assert.isTrue(file.exists(), () -> "脚本出参中的 " + child.getName() + " 指定的文件不存在: " + value);
      return new FileSystemResource(file);
    }

    // 特殊处理文件列表，值当作本地文件路径列表
    if (child.isList() && child.getArrayElement() != null && child.getArrayElement().getType() == AttrDataType.FILE) {
      return resolveDownloadedFiles(tmpDir, child.getName(), value);
    }

    // 转换普通出参
    try {
      return ParamConverterUtil.convert(child.getName(), child, value);
    }
    catch (BssException e) {
      // 调整异常信息，以避免用户误以为是脚本入参问题
      e.setFailMsg("脚本出参转换失败: " + e.getFailMsg());
      throw e;
    }
    catch (Exception e) {
      throw new BssException("脚本出参转换失败: " + ExpUtil.getMsg(e), e);
    }
  }

  /**
   * 解析下载的文件列表
   */
  @SuppressWarnings("unchecked")
  private List<Resource> resolveDownloadedFiles(File tmpDir, String attr, @Nullable Object files) {
    if (files == null) {
      return List.of();
    }
    Assert.isTrue(files instanceof List, () -> "脚本出参中的 " + attr + " 必须是数组类型，实际是: " + files.getClass().getName());
    List<?> fileList = (List<?>) files;
    if (fileList.isEmpty()) {
      return List.of();
    }
    Assert.isTrue(fileList.stream().allMatch(filepath -> filepath instanceof String), () -> "脚本出参中的 " + attr + " 必须是字符串数组");
    List<Resource> downloadedFiles = new ArrayList<>();
    for (String filepath : (List<String>) files) {
      Assert.hasLength(filepath, () -> "脚本出参中的 " + attr + " 元素不能是空字符串");
      File file = tmpDir.toPath().resolve(filepath).toFile();
      Assert.isTrue(file.exists(), () -> "脚本出参中的 " + attr + " 指定的文件不存在: " + filepath);
      downloadedFiles.add(new FileSystemResource(file));
    }
    return downloadedFiles;
  }

  /**
   * 上传文件到临时目录
   */
  private List<String> uploadFiles(File tmpDir, PlaywrightStep step) {
    List<AbstractFile> files = resolveFiles(step.getFiles());
    if (files.isEmpty()) {
      return List.of();
    }

    File uploadDir = new File(tmpDir, "upload");
    if (!uploadDir.exists()) {
      Assert.isTrue(uploadDir.mkdir(), "创建上传目录失败");
    }
    List<String> filePaths = new ArrayList<>();
    for (int i = 0; i < files.size(); i++) {
      AbstractFile file = files.get(i);
      String filename = file.getFilename();
      if (filename == null) {
        filename = "file_" + (i + 1);
      }
      File uploadedFile = new File(uploadDir, filename);
      writeUploadedFile(step, file, uploadedFile);
      filePaths.add("upload" + File.separator + filename);
    }
    return filePaths;
  }

  /**
   * 写入上传文件
   */
  private void writeUploadedFile(PlaywrightStep step, AbstractFile file, File uploadedFile) {
    //noinspection IfCanBeSwitch
    if (file instanceof UrlFile urlFile) {
      try {
        HttpUtil.getRestTemplate().execute(urlFile.getUrl().toString(), HttpMethod.GET, null, response -> {
          FileUtils.copyInputStreamToFile(response.getBody(), uploadedFile);
          return null;
        });
      }
      catch (Exception e) {
        logger.error("Failed to download file: step={}, url={}", step.getCode(), urlFile.getUrl(), e);
        throw new BssException("根据 URL 下载文件失败: url=" + urlFile.getUrl() + ", error={}" + ExpUtil.getMsg(e), e);
      }
    }
    else if (file instanceof DataUrlFile dataUrlFile) {
      try {
        FileUtils.writeByteArrayToFile(uploadedFile, Base64.getDecoder().decode(dataUrlFile.getFileContent()));
      }
      catch (Exception e) {
        logger.error("Failed to write uploaded file: step={}, file={}", step.getCode(), uploadedFile);
        throw new BssException("写入上传文件失败: " + ExpUtil.getMsg(e), e);
      }
    }
    else if (file instanceof FileServerFile fileServerFile) {
      fileStoreService.downloadFile(fileServerFile.getFileInfo().getFileId(), uploadedFile.getAbsolutePath());
    }
    else {
      throw new BssException("未知的文件类型：" + file.getClass().getName());
    }
  }

  /**
   * 发送开始事件
   */
  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  private void sendPlaywrightStartEvent(SceneOrchestrationContext context, PlaywrightStep step, String userCode, String chatId, Optional<OrchestrationStepRunLog> logOptional) {
    boolean vncEnabled = !Boolean.FALSE.equals(step.getVncEnabled());
    if (vncEnabled) {
      String vncUrl = agentPoolClient.getVncUrl(userCode, chatId, Boolean.TRUE.equals(step.getVncReadonly()));
      logOptional.ifPresent(log -> log.addLog("vnc url: %s", vncUrl));
      // 任务型工作流没有 replyHandler
      if (context.getReplyHandler() != null) {
        PlaywrightAutomationEvent event = new PlaywrightAutomationEvent("start", vncUrl);
        context.getReplyHandler().reply(ChatMessageType.PLAYWRIGHT_AUTOMATION, event, step.getCode(), step.getName());
      }
    }
  }

  /**
   * 发送结束事件
   */
  private void sendPlaywrightStopEvent(SceneOrchestrationContext context, PlaywrightStep step) {
    boolean vncEnabled = !Boolean.FALSE.equals(step.getVncEnabled());
    if (vncEnabled && context.getReplyHandler() != null) {
      PlaywrightAutomationEvent event = new PlaywrightAutomationEvent("stop", null);
      context.getReplyHandler().reply(ChatMessageType.PLAYWRIGHT_AUTOMATION, event, step.getCode(), step.getName());
    }
  }

  /**
   * PlayWright 自动化事件
   *
   * @param action 动作
   * @param vncUrl VNC 页面地址
   */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record PlaywrightAutomationEvent(String action, @Nullable String vncUrl) {
  }
}
