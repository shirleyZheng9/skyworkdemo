package com.iwhalecloud.bote.service.orchestration.runner.step;

import com.google.common.collect.ImmutableMap;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.ChatConsts;
import com.iwhalecloud.bote.common.consts.SceneConsts;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.orchestration.log.OrchestrationStepRunLog;
import com.iwhalecloud.bote.dto.orchestration.step.AgentStep.FileProcessingStrategy;
import com.iwhalecloud.bote.dto.orchestration.step.McpStep;
import com.iwhalecloud.bote.dto.scene.SceneChatParamsDTO;
import com.iwhalecloud.bote.entity.mcp.McpToolFileEntity;
import com.iwhalecloud.bote.mapper.mcp.McpToolFileMapper;
import com.iwhalecloud.bote.mcp.client.McpClient;
import com.iwhalecloud.bote.mcp.dto.Content;
import com.iwhalecloud.bote.mcp.dto.ImageContent;
import com.iwhalecloud.bote.mcp.dto.TextContent;
import com.iwhalecloud.bote.mcp.dto.request.CallToolRequest;
import com.iwhalecloud.bote.mcp.dto.response.CallToolResult;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreService;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import com.iwhalecloud.bss.litchi.file.vo.UploadConfigVO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

/**
 * MCP 工具步骤执行器
 *
 * @author bianjp
 * @since 2025-05-23
 */
public class McpStepRunner extends AbstractLlmStepRunner<McpStep> {
  private static final IFileStoreService fileStoreService = SpringUtil.getBean(IFileStoreService.class);
  private static final McpToolFileMapper mcpToolFileMapper = SpringUtil.getBean(McpToolFileMapper.class);
  /** 文件上传子目录 */
  private static final String UPLOAD_SUB_FOLDER = "mcp-tool";
  /** mime-type -> 文件扩展名映射，只维护常见图片类型 */
  private static final Map<String, String> mimeTypeToExtensionMap = ImmutableMap.<String, String>builder()
    .put("image/apng", "apng")
    .put("image/bmp", "bmp")
    .put("image/gif", "gif")
    .put("image/jpeg", "jpg")
    .put("image/svg+xml", "svg")
    .put("image/webp", "webp")
    .build();

  @Override
  public Object runAsTool(SceneChatParamsDTO sceneChatParams, McpStep step, String toolCallId, @Nullable Map<String, Object> toolArguments, Optional<OrchestrationStepRunLog> log) {
    McpClient client = step.getClient();
    log.ifPresent(l -> {
      Map<String, Object> input = new LinkedHashMap<>();
      input.put("tool", step.getToolName());
      input.put("parameters", toolArguments);
      l.setInput(input);
    });

    // 不检查是否调用成功，让大模型判断
    CallToolResult result = client.callTool(new CallToolRequest(step.getToolName(), toolArguments));

    // 处理文件。文件类型目前只有图片
    FileProcessingStrategy strategy = ObjectUtils.getIfNull(step.getFileProcessingStrategy(), FileProcessingStrategy.NONE);
    // 只在有文件时处理
    if (strategy != FileProcessingStrategy.NONE && IterableUtils.matchesAny(result.getContent(), c -> c instanceof ImageContent)) {
      // 丢弃文件
      if (strategy == FileProcessingStrategy.DROP) {
        result.setContent(result.getContent().stream().filter(c -> c instanceof TextContent).collect(Collectors.toList()));
      }
      // 文件内容替换为链接
      else if (strategy == FileProcessingStrategy.URL) {
        result.setContent(replaceFileWithUrl(sceneChatParams, step, result.getContent()));
      }
    }
    // 转为 Map 类型以方便其它节点引用
    return JsonUtil.convert(result, Object.class);
  }

  /**
   * 文件内容替换为链接
   */
  private List<Content> replaceFileWithUrl(SceneChatParamsDTO sceneChatParams, McpStep step, List<Content> contentList) {
    List<Content> processedContentList = new ArrayList<>(contentList.size());
    String urlPrefix = Boolean.TRUE.equals(sceneChatParams.getDebug()) ? "../api/bote/file/file/id/" : "api/bote/file/file/id/";
    int count = 0;
    for (Content content : contentList) {
      if (!(content instanceof ImageContent)) {
        processedContentList.add(content);
        continue;
      }
      ImageContent imageContent = (ImageContent) content;
      if (StringUtils.isEmpty(imageContent.getData())) {
        processedContentList.add(content);
        continue;
      }

      count++;
      byte[] bytes = Base64.decodeBase64(imageContent.getData());
      String fileType = mimeTypeToExtensionMap.getOrDefault(imageContent.getMimeType(), "png");
      UploadConfigVO uploadConfig = new UploadConfigVO();
      uploadConfig.setSubFolder(UPLOAD_SUB_FOLDER + "/" + sceneChatParams.getTenantId());
      uploadConfig.setIsPicture(true);
      uploadConfig.setFileSize((long) bytes.length);
      uploadConfig.setFileType(fileType);
      uploadConfig.setOriginalFileName(step.getToolName() + "-" + count + "." + fileType);
      FileInfoVO fileInfo = fileStoreService.uploadFile(bytes, uploadConfig);
      CustomImageContent customImageContent = new CustomImageContent();
      customImageContent.setAudience(imageContent.getAudience());
      customImageContent.setPriority(imageContent.getPriority());
      customImageContent.setMimeType(imageContent.getMimeType());
      customImageContent.setUrl(urlPrefix + fileInfo.getFileId());
      processedContentList.add(customImageContent);

      // 记录到数据库
      McpToolFileEntity entity = new McpToolFileEntity();
      entity.setId(IDUtils.nextId());
      entity.setTenantId(sceneChatParams.getTenantId());
      entity.setFileId(fileInfo.getFileId());
      // 没有会话 ID 时当作调试，以便能自动清理文件
      if (sceneChatParams.getConversationId() == null || ChatConsts.DEFAULT_SESSION_ID.equals(sceneChatParams.getConversationId())) {
        entity.setSessionId(SceneConsts.TEST_CONVERSATION_ID);
      }
      else {
        entity.setSessionId(sceneChatParams.getConversationId());
      }
      entity.setTransactionId(sceneChatParams.getTransactionId());
      entity.setContextId(sceneChatParams.getContextId());
      entity.setMcpServerId(step.getServerId());
      entity.setMcpToolName(step.getToolName());
      entity.setStatusCd(BaseConsts.STATUS_CD_VALID);
      entity.setCreatorId(SessionUtil.getOptionalUserId());
      mcpToolFileMapper.insertMcpToolFile(entity);
    }
    return processedContentList;
  }

  /**
   * 自定义图片内容，增加 url
   */
  @Getter
  @Setter
  @ToString(callSuper = true)
  public static class CustomImageContent extends ImageContent {
    /** 文件链接 */
    private String url;
  }
}
