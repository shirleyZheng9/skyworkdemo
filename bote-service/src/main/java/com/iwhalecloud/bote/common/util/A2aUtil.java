package com.iwhalecloud.bote.common.util;

import com.iwhalecloud.bote.common.consts.A2aConsts;
import com.iwhalecloud.bote.dto.a2a.A2aAuthConfig;
import com.iwhalecloud.bote.dto.a2a.A2aPlatformDTO;
import com.iwhalecloud.bote.dto.a2a.A2aTaskInfo;
import com.iwhalecloud.bote.dto.orchestration.file.AbstractFile;
import com.iwhalecloud.bote.dto.orchestration.file.DataUrlFile;
import com.iwhalecloud.bote.dto.orchestration.file.FileServerFile;
import com.iwhalecloud.bote.dto.orchestration.file.UrlFile;
import com.iwhalecloud.bote.llm.client.dto.HeaderItem;
import com.iwhalecloud.bote.mapper.a2a.A2aPlatformMapper;
import com.iwhalecloud.bote.service.orchestration.runner.step.ToolboxStepRunner;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreService;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import com.iwhalecloud.bss.litchi.file.vo.UploadConfigVO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import io.a2a.spec.APIKeySecurityScheme;
import io.a2a.spec.AgentCard;
import io.a2a.spec.Artifact;
import io.a2a.spec.DataPart;
import io.a2a.spec.FileContent;
import io.a2a.spec.FilePart;
import io.a2a.spec.FileWithBytes;
import io.a2a.spec.FileWithUri;
import io.a2a.spec.HTTPAuthSecurityScheme;
import io.a2a.spec.Message;
import io.a2a.spec.Part;
import io.a2a.spec.SecurityScheme;
import io.a2a.spec.Task;
import io.a2a.spec.TextPart;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.web.client.HttpStatusCodeException;

/**
 * A2A 工具类
 *
 * @author bianjp
 * @since 2025-09-09
 */
@SuppressWarnings("PMD.GuardLogStatement")
public final class A2aUtil {
  private static final Logger logger = LoggerFactory.getLogger(A2aUtil.class);
  /** 最大文件大小 */
  private static final int MAX_FILE_SIZE = 1024 * 1024 * 100;
  private static final A2aPlatformMapper platformMapper = SpringUtil.getBean(A2aPlatformMapper.class);
  private static final IFileStoreService fileStoreService = SpringUtil.getBean(IFileStoreService.class);

  private A2aUtil() {
  }

  /**
   * 解析智能体卡片地址
   *
   * @param url 卡片地址，必须是完整地址（必要时包含 .well-known/agent-card.json）
   * @return 基础地址和路径
   */
  public static AgentCard getAgentCard(String url, @Nullable Map<String, String> authHeaders) {
    Assert.hasLength(url, "智能体卡片地址不能为空");
    Assert.isTrue(HttpUtil.isValid(url), "智能体卡片地址不合法");

    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(List.of(MediaType.APPLICATION_JSON));
    if (MapUtils.isNotEmpty(authHeaders)) {
      authHeaders.forEach(headers::add);
    }
    AgentCard agentCard = HttpUtil.get(url, null, ParameterizedTypeReference.forType(AgentCard.class), headers);
    Assert.notNull(agentCard, "获取智能体卡片失败，响应为空");
    return agentCard;
  }

  /**
   * 获取智能体图标
   */
  @Nullable
  public static String getAgentIcon(AgentCard card) {
    String url = card.iconUrl();
    if (!HttpUtil.isValid(url)) {
      return null;
    }
    // 图标不是很重要，获取失败时返回 null
    try {
      return HttpUtil.getRestTemplate().execute(url, HttpMethod.GET, null, response -> {
        try (response) {
          return parseIcon(url, response);
        }
        catch (Exception e) {
          logger.warn("Failed to get a2a agent icon: url={}", url, e);
        }
        return null;
      });
    }
    catch (Exception e) {
      logger.warn("Failed to get a2a agent icon: url={}", url, e);
      return null;
    }
  }

  /**
   * 解析图标，返回 dataUri 形式
   */
  @Nullable
  private static String parseIcon(String url, ClientHttpResponse response) throws IOException {
    if (response.getStatusCode() != HttpStatus.OK) {
      logger.warn("Failed to get a2a agent icon: url={}, status={}", url, response.getStatusCode().value());
      return null;
    }
    MediaType contentType = response.getHeaders().getContentType();
    if (contentType == null) {
      logger.warn("Invalid a2a agent icon, missing content-type: url={}", url);
      return null;
    }
    if (!MediaType.IMAGE_PNG.isCompatibleWith(contentType) && !MediaType.IMAGE_JPEG.isCompatibleWith(contentType)) {
      logger.warn("Invalid a2a agent icon type: url={}, contentType={}", url, contentType);
      return null;
    }
    byte[] bytes = IOUtils.toByteArray(response.getBody());
    return "data:image/" + contentType.getSubtype() + ";base64," + Base64.encodeBase64String(bytes);
  }

  /**
   * 获取智能体卡片声明的鉴权请求头
   *
   * @param agentCard 智能体卡片
   * @return 鉴权请求头名称列表
   */
  public static List<HeaderItem> getAuthHeaders(AgentCard agentCard) {
    if (MapUtils.isEmpty(agentCard.securitySchemes())) {
      return List.of();
    }
    List<HeaderItem> headers = new ArrayList<>();
    for (SecurityScheme scheme : agentCard.securitySchemes().values()) {
      if (scheme instanceof HTTPAuthSecurityScheme httpAuthSecurityScheme) {
        if (headers.stream().noneMatch(h -> HttpHeaders.AUTHORIZATION.equalsIgnoreCase(h.getName()))) {
          String description = StringUtils.defaultIfEmpty(scheme.getDescription(), "授权");
          if (StringUtils.isNotEmpty(httpAuthSecurityScheme.getScheme())) {
            description += "(" + httpAuthSecurityScheme.getScheme() + ")";
          }
          headers.add(new HeaderItem(HttpHeaders.AUTHORIZATION, null, description));
        }
      }
      else if (scheme instanceof APIKeySecurityScheme apiKeySecurityScheme) {
        if ("header".equalsIgnoreCase(apiKeySecurityScheme.getIn())
          && headers.stream().noneMatch(h -> h.getName().equalsIgnoreCase(apiKeySecurityScheme.getName()))) {
          headers.add(new HeaderItem(apiKeySecurityScheme.getName(), null, apiKeySecurityScheme.getDescription()));
        }
      }
    }
    return headers;
  }

  /**
   * 提取消息中的文本内容
   */
  public static String extractTextFromMessage(Message message) {
    if (CollectionUtils.isEmpty(message.getParts())) {
      return "";
    }
    return message.getParts().stream()
      .filter(part -> part instanceof TextPart)
      .map(part -> ((TextPart) part).getText())
      .filter(StringUtils::isNotEmpty)
      .collect(Collectors.joining("\n\n"));
  }

  /**
   * 提取消息中的结构化数据
   */
  @Nullable
  public static Map<String, Object> extractDataFromMessage(Message message) {
    if (CollectionUtils.isEmpty(message.getParts())) {
      return null;
    }
    // 合并所有 DataPart
    Map<String, Object> data = new LinkedHashMap<>();
    for (Part<?> part : message.getParts()) {
      if (part instanceof DataPart dataPart && MapUtils.isNotEmpty(dataPart.getData())) {
        data.putAll(dataPart.getData());
      }
    }
    return data.isEmpty() ? null : data;
  }

  /**
   * 提取消息中的文件，上传到文件服务器
   *
   * @return 文件 ID 列表
   */
  @Nullable
  public static List<Long> extractFilesFromMessage(Message message) {
    if (CollectionUtils.isEmpty(message.getParts())) {
      return null;
    }
    List<Long> fileIds = new ArrayList<>();
    for (Part<?> part : message.getParts()) {
      if (!(part instanceof FilePart filePart)) {
        continue;
      }
      FileContent fileContent = filePart.getFile();
      UploadConfigVO uploadConfig = new UploadConfigVO();
      uploadConfig.setSubFolder("a2a-message");
      uploadConfig.setOriginalFileName(fileContent.name());
      uploadConfig.setFileType(detectFileType(fileContent));
      uploadConfig.setDescription("A2A 消息, taskId=" + message.getTaskId());
      FileInfoVO fileInfo;
      // URL 形式，下载并上传到文件服务器
      if (fileContent instanceof FileWithUri fileWithUri) {
        fileInfo = downloadA2aFile(fileWithUri.uri(), uploadConfig);
      }
      // Bytes 形式，解析 base64 上传到文件服务器
      else if (fileContent instanceof FileWithBytes fileWithBytes) {
        byte[] bytes = Base64.decodeBase64(fileWithBytes.bytes());
        Assert.isTrue(bytes != null && bytes.length > 0, () -> "文件不能为空: " + fileContent.name());
        fileInfo = fileStoreService.uploadFile(bytes, uploadConfig);
      }
      else {
        throw new BssException("未知的 A2A 文件类型: " + fileContent.getClass().getName());
      }
      fileIds.add(fileInfo.getFileId());
    }
    return fileIds.isEmpty() ? null : fileIds;
  }

  /**
   * 探测 A2A 文件类型
   */
  @Nullable
  private static String detectFileType(FileContent fileContent) {
    // 优先使用文件扩展名
    String extension = FilenameUtils.getExtension(fileContent.name());
    // 其次根据媒体类型获取扩展名
    if (StringUtils.isEmpty(extension) && StringUtils.isNotEmpty(fileContent.mimeType())) {
      try {
        MimeType mimeType = MimeTypes.getDefaultMimeTypes().forName(fileContent.mimeType());
        extension = StringUtils.removeStart(mimeType.getExtension(), '.');
      }
      catch (Exception e) {
        // 文件类型不太重要，忽略异常
      }
    }
    return StringUtils.isNotEmpty(extension) ? extension : null;
  }

  /**
   * 下载 A2A 消息中的文件，上传到文件服务器
   */
  private static FileInfoVO downloadA2aFile(String url, UploadConfigVO uploadConfig) {
    FileInfoVO fileInfo;
    logger.debug("Downloading a2a file: url={}", url);
    try {
      fileInfo = HttpUtil.getRestTemplate().execute(url, HttpMethod.GET, null, response -> {
        if (response.getStatusCode().value() != 200) {
          logger.error("Failed to download a2a file: url={}, status={}", url, response.getStatusCode().value());
          throw new BssException("下载 A2A 消息文件失败: url=" + url + ", status=" + response.getStatusCode().value());
        }
        try (InputStream inputStream = response.getBody()) {
          return fileStoreService.uploadFile(inputStream, uploadConfig);
        }
      });
    }
    catch (BssException e) {
      throw e;
    }
    catch (HttpStatusCodeException e) {
      int statusCode = e.getStatusCode().value();
      String responseBody = e.getResponseBodyAsString();
      logger.error("Failed to download a2a file: url={}, status={}, response={}", url, statusCode, responseBody, e);
      throw new BssException("下载 A2A 消息文件失败: url=" + url + ", status=" + statusCode + ", response=" + responseBody, e);
    }
    catch (Exception e) {
      logger.error("Failed to download a2a file: url={}", url, e);
      throw new BssException("下载 A2A 消息文件失败: url=" + url + ", error=" + ExpUtil.getMsg(e), e);
    }
    Assert.notNull(fileInfo, () -> "下载 A2A 消息文件失败: url=" + url);
    return fileInfo;
  }

  /**
   * 提取产物中的文本内容
   */
  public static String extractTextFromArtifact(Artifact artifact) {
    if (CollectionUtils.isEmpty(artifact.parts())) {
      return "";
    }
    return artifact.parts().stream()
      .map(A2aUtil::convertPartToText)
      .filter(StringUtils::isNotEmpty)
      .collect(Collectors.joining("\n\n"));
  }

  /**
   * 将产物中的部分转换为文本
   */
  @Nullable
  @SuppressWarnings({"IfCanBeSwitch", "PMD.UnusedPrivateMethod"})
  private static String convertPartToText(Part<?> part) {
    // 文本
    if (part instanceof TextPart textPart) {
      return textPart.getText();
    }
    // 文件
    else if (part instanceof FilePart filePart) {
      FileContent file = filePart.getFile();
      // 只处理 uri 类型。base64 可能太大，不适合添加到文本中
      if (file instanceof FileWithUri fileWithUri && StringUtils.isNotEmpty(fileWithUri.uri())) {
        if (StringUtils.isNotEmpty(file.name())) {
          return "文件: " + file.name() + " , 链接: " + fileWithUri.uri();
        }
        return "文件: " + fileWithUri.uri();
      }
    }
    // 数据
    else if (part instanceof DataPart dataPart && MapUtils.isNotEmpty(dataPart.getData())) {
      return JsonUtil.toJsonString(dataPart.getData());
    }
    return null;
  }

  /**
   * 提取任务中的文本内容
   */
  public static String extractReplyFromTask(Task task) {
    // 优先取任务状态中的消息
    Message message = task.getStatus().message();
    if (message != null) {
      return extractTextFromMessage(message);
    }
    // 其次取 artifact 中的文本
    List<Artifact> artifacts = task.getArtifacts();
    if (CollectionUtils.isNotEmpty(artifacts)) {
      return artifacts.stream()
        .map(A2aUtil::extractTextFromArtifact)
        .filter(StringUtils::isNotEmpty)
        // 不同 artifact 的文本用两个换行符分隔
        .collect(Collectors.joining("\n\n"));
    }
    return "";
  }

  /**
   * 构造用户消息
   */
  public static Message buildUserMessage(@Nullable String text, @Nullable Map<String, Object> data, @Nullable List<FilePart> files,
                                         @Nullable Map<String, Object> metadata, A2aTaskInfo taskInfo) {
    List<Part<?>> parts = new ArrayList<>();
    if (StringUtils.isNotEmpty(text)) {
      parts.add(new TextPart(text));
    }
    if (data != null && !data.isEmpty()) {
      parts.add(new DataPart(data));
    }
    if (files != null && !files.isEmpty()) {
      parts.addAll(files);
    }
    return new Message.Builder()
      .role(Message.Role.USER)
      .parts(parts)
      .metadata(metadata)
      // 传递未结束的任务 ID, 以及当前会话关联的 contextId。没有任务 ID 时也可能有 contextId
      .taskId(taskInfo.getTaskId())
      .contextId(taskInfo.getContextId())
      .build();
  }

  /**
   * 根据文件 ID 列表构造消息的文件列表
   */
  @Nullable
  public static List<FilePart> buildMessageFilesByFileIds(@Nullable List<Long> fileIds) {
    if (CollectionUtils.isEmpty(fileIds)) {
      return null;
    }

    List<AbstractFile> files = new ArrayList<>(fileIds.size());
    for (Long fileId : fileIds) {
      FileInfoVO fileInfo = fileStoreService.getFileInfoById(fileId);
      Assert.notNull(fileInfo, () -> "文件不存在: " + fileId);
      files.add(new FileServerFile(fileInfo));
    }
    return buildMessageFiles(files);
  }

  /**
   * 构造消息的文件列表
   */
  public static List<FilePart> buildMessageFiles(List<AbstractFile> files) {
    List<FilePart> fileParts = new ArrayList<>(files.size());
    for (AbstractFile file : files) {
      switch (file) {
        case UrlFile urlFile -> {
          FileContent content = new FileWithUri(urlFile.getMimeType(), urlFile.getFilename(), urlFile.getUrl().toString());
          fileParts.add(new FilePart(content));
        }
        case DataUrlFile dataUrlFile -> {
          FileContent content = new FileWithBytes(dataUrlFile.getMimeType(), dataUrlFile.getFilename(), dataUrlFile.getFileContent());
          fileParts.add(new FilePart(content));
        }
        case FileServerFile fileServerFile -> fileParts.add(buildFilePartByFileId(fileServerFile));
        default -> throw new BssException("不支持的文件类型: " + file.getClass().getName());
      }
    }
    return fileParts;
  }


  /**
   * 根据文件 ID 构建 FilePart
   */
  private static FilePart buildFilePartByFileId(FileServerFile file) {
    FileInfoVO fileInfo = file.getFileInfo();
    Long fileId = fileInfo.getFileId();
    // 限制文件大小
    Assert.isTrue(fileInfo.getFileSize() == null || fileInfo.getFileSize() <= MAX_FILE_SIZE, () -> "文件大小超出限制: " + fileId);

    // 下载文件
    byte[] bytes = fileStoreService.downloadFile(fileId);
    Assert.isTrue(bytes != null && bytes.length > 0, () -> "文件不存在: " + fileId);
    return new FilePart(new FileWithBytes(file.getMimeType(), file.getFilename(), Base64.encodeBase64String(bytes)));
  }

  /**
   * 获取 A2A 平台的鉴权请求头
   */
  public static Map<String, String> getPlatformHeaders(@Nullable Long tenantId, @Nullable Long platformId) {
    if (tenantId == null || platformId == null || A2aConsts.DEFAULT_A2A_PLATFORM_ID.equals(platformId)) {
      return Map.of();
    }

    A2aPlatformDTO platform = platformMapper.selectPlatformAuthConfig(tenantId, platformId);
    Assert.notNull(platform, () -> "A2A 平台不存在: platformId=" + platformId);
    platform.parseJsonConfig();

    // 请求头，忽略大小写
    Map<String, String> headers = new CaseInsensitiveMap<>();
    // 添加固定请求头
    fillHeaders(headers, platform.getAuthConfig());
    // 解析动态请求头
    if (platform.getAuthExtFuncId() != null) {
      resolveDynamicHeaders(tenantId, platform.getAuthExtFuncId(), headers);
    }

    return headers;
  }

  /**
   * 解析动态请求头
   */
  @SuppressWarnings("rawtypes") // 试飞构建用的 lizard 有 bug, result instanceof Map<?, ?> 会错误计算为 CCN=2
  private static void resolveDynamicHeaders(Long tenantId, Long funcId, Map<String, String> headers) {
    Object result = ToolboxStepRunner.invokeToolbox(tenantId, funcId, null, null);
    // 响应不合法时忽略
    if (!(result instanceof Map resultMap)) {
      return;
    }
    Object generatedHeaders = resultMap.get("headers");
    if (!(generatedHeaders instanceof Map<?, ?> generatedHeadersMap)) {
      return;
    }
    for (Map.Entry<?, ?> entry : generatedHeadersMap.entrySet()) {
      String key = Objects.toString(entry.getKey(), null);
      String value = Objects.toString(entry.getValue(), null);
      if (StringUtils.isNotEmpty(key) && StringUtils.isNotEmpty(value)) {
        headers.put(key, value);
      }
    }
  }

  /**
   * 添加鉴权配置中的请求头
   */
  public static void fillHeaders(Map<String, String> headers, @Nullable A2aAuthConfig authConfig) {
    if (authConfig == null || CollectionUtils.isEmpty(authConfig.getHeaders())) {
      return;
    }
    for (HeaderItem header : authConfig.getHeaders()) {
      if (StringUtils.isNotEmpty(header.getName()) && StringUtils.isNotBlank(header.getValue())) {
        headers.put(header.getName(), StringUtils.trim(header.getValue()));
      }
    }
  }
}
