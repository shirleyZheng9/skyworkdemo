package com.iwhalecloud.bote.service.plugin.runner;

import com.iwhalecloud.bote.cache.ModelClientCache;
import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.util.PluginContextUtil;
import com.iwhalecloud.bote.common.util.SceneParamUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.PluginExecutionContext;
import com.iwhalecloud.bote.dto.plugin.params.ImageAnalysisPluginParams;
import com.iwhalecloud.bote.llm.client.LlmClient;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionRequest;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionResponse;
import com.iwhalecloud.bote.llm.client.dto.message.ImageUrl;
import com.iwhalecloud.bote.llm.client.dto.message.MessageContent;
import com.iwhalecloud.bote.llm.client.dto.message.SystemMessage;
import com.iwhalecloud.bote.llm.client.dto.message.UserMessage;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreService;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * AI识图插件
 *
 * @author zhangJun
 * @since 2025-07-18
 */
@Component
public class ImageAnalysisPlugin extends AbstractPlugin<ImageAnalysisPluginParams> {
  private final ModelClientCache modelClientCache;
  private final IFileStoreService fileStoreService;

  public static final String PROMPT = "你是一位专业的视觉分析专家，根据用户上传的图片与问题描述，回答问题返回结果。";

  public ImageAnalysisPlugin(ModelClientCache modelClientCache, IFileStoreService fileStoreService) {
    super(ImageAnalysisPluginParams.class);
    this.modelClientCache = modelClientCache;
    this.fileStoreService = fileStoreService;
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_IMAGE_ANALYSIS;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("prompt", "提示词描述", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("fileId", "文件ID", AttrDataType.NUMBER));
    return ParameterSpec.newRoot(children);
  }

  @Override
  public ParameterSpec createResponseParameter() {
    return ParameterSpec.newRoot(Collections.singletonList(ParameterSpec.newProperty("content", "大模型返回的内容", AttrDataType.STRING)));
  }

  @Override
  public void validateParams(ImageAnalysisPluginParams params) {
    Assert.hasText(params.getPrompt(), "提示词描述不能为空！");
    Assert.notNull(params.getFileId(), "文件ID不能为空！");
  }

  @Override
  public Object doRun(ImageAnalysisPluginParams pluginParams) {
    Map<String, Object> params = new HashMap<>();
    PluginExecutionContext pluginContext = PluginContextUtil.getContext();
    // 获取模型 ID
    LlmClient modelClient = modelClientCache.getLlmClient(pluginContext.getTenantId(), pluginContext.getModelId());
    // 解析图片
    MessageContent imageMessageContents = buildFileMessageContentByFileId(pluginParams.getFileId());
    List<MessageContent> contentList = new ArrayList<>();
    // 图片内容
    contentList.add(imageMessageContents);
    // 提示词
    Object content = SceneParamUtil.getParamValue(pluginParams.getPrompt());
    contentList.add(new MessageContent(String.valueOf(content)));
    UserMessage userMessage = new UserMessage(contentList);
    SystemMessage systemMessage = new SystemMessage(PROMPT);
    // 大模型调用
    ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
      .tenantId(pluginContext.getTenantId())
      .addMessage(userMessage)
      .addMessage(systemMessage).build();
    ChatCompletionResponse response = modelClient.chatCompletion(chatCompletionRequest);
    // 获取响应内容
    params.put("content", response.getMessageContent());
    return params;
  }


  /**
   * 根据文件 ID 构造文件消息内容
   */
  private MessageContent buildFileMessageContentByFileId(Long fileId) {
    FileInfoVO fileInfo = fileStoreService.getFileInfoById(fileId);
    Assert.notNull(fileInfo, () -> "文件不存在: " + fileId);
    // 限制文件大小
    Assert.isTrue(fileInfo.getFileSize() == null || fileInfo.getFileSize() <= 10 * 1024 * 1024, () -> "文件大小超出限制: " + fileId);
    // 获取文件类型
    String fileType = fileInfo.getFileType();
    if (StringUtils.isEmpty(fileType) && StringUtils.isNotEmpty(fileInfo.getFileName())) {
      fileType = FilenameUtils.getExtension(fileInfo.getFileName());
    }
    Assert.hasLength(fileType, () -> "文件类型未知: " + fileId);
    String mimeType = getMimeType(fileType);
    // 下载文件，构造 data URI 字符串
    byte[] bytes = fileStoreService.downloadFile(fileId);
    Assert.isTrue(bytes != null && bytes.length > 0, () -> "文件不存在: " + fileId);
    String dataUri = "data:" + mimeType + ";base64," + Base64.encodeBase64String(bytes);
    return new MessageContent(new ImageUrl(dataUri));
  }

  private static String getMimeType(String fileType) {
    String mimeType;
    switch (fileType.toLowerCase()) {
      case "png":
      case "apng":
        mimeType = "image/png";
        break;
      case "jpg":
      case "jpeg":
        mimeType = "image/jpeg";
        break;
      case "webp":
        mimeType = "image/webp";
        break;
      case "bmp":
        mimeType = "image/bmp";
        break;
      default:
        throw new BssException("不支持的图片格式: " + fileType);
    }
    return mimeType;
  }

}
