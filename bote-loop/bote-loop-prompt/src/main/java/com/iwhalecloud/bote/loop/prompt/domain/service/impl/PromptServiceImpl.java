package com.iwhalecloud.bote.loop.prompt.domain.service.impl;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iwhalecloud.bote.common.util.ModelClientUtil;
import com.iwhalecloud.bote.common.util.ModelConfigUtil;
import com.iwhalecloud.bote.dto.model.SimpleLargeModelDTO;
import com.iwhalecloud.bote.llm.client.LlmClient;
import com.iwhalecloud.bote.llm.client.config.LlmProperties;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionRequest;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionResponse;
import com.iwhalecloud.bote.loop.prompt.application.convertor.LlmConvertor;
import com.iwhalecloud.bote.loop.prompt.domain.entity.ContentPart;
import com.iwhalecloud.bote.loop.prompt.domain.entity.Message;
import com.iwhalecloud.bote.loop.prompt.domain.entity.ModelConfig;
import com.iwhalecloud.bote.loop.prompt.domain.entity.Prompt;
import com.iwhalecloud.bote.loop.prompt.domain.entity.PromptBasic;
import com.iwhalecloud.bote.loop.prompt.domain.entity.PromptCommit;
import com.iwhalecloud.bote.loop.prompt.domain.entity.PromptDetail;
import com.iwhalecloud.bote.loop.prompt.domain.entity.PromptDraft;
import com.iwhalecloud.bote.loop.prompt.domain.entity.Reply;
import com.iwhalecloud.bote.loop.prompt.domain.entity.VariableVal;
import com.iwhalecloud.bote.loop.prompt.domain.repo.IManageRepo;
import com.iwhalecloud.bote.loop.prompt.domain.repo.dto.ExecuteParam;
import com.iwhalecloud.bote.loop.prompt.domain.repo.dto.PromptKeyVersionPair;
import com.iwhalecloud.bote.loop.prompt.domain.repo.dto.PromptOptions;
import com.iwhalecloud.bote.loop.prompt.domain.service.IPromptService;
import com.iwhalecloud.bote.mapper.model.LargeModelManageMapper;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreService;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

/**
 * Prompt领域服务实现
 * 迁移对应关系: Go语言modules/prompt/domain/service.PromptServiceImpl
 * - 功能: 核心Prompt业务逻辑服务实现
 * - 依赖注入:
 * * idGenerator - ID生成器
 * * debugLogRepo - 调试日志仓库
 * * debugContextRepo - 调试上下文仓库
 * * manageRepo - 管理仓库
 * * configProvider - 配置提供者
 * * llmProvider - LLM提供者
 * * fileProvider - 文件提供者
 * <p>
 * Java实现说明:
 * - 对应Go的service.PromptServiceImpl结构体
 * - 使用Spring的@Service注解进行依赖注入
 * - 实现IPromptService接口的所有方法
 * - 使用Spring的异常处理机制
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go依赖注入 -> Spring依赖注入
 * - Go error处理 -> Java异常处理
 * - Go context.Context -> Java方法参数
 */
@Service
@RequiredArgsConstructor
public class PromptServiceImpl implements IPromptService {
  private final IManageRepo manageRepo;
  private final LargeModelManageMapper largeModelManageMapper;
  private final IFileStoreService fileStoreService;

  @Override
  public List<Message> formatPrompt(Prompt prompt, List<Message> messages, List<VariableVal> variableVals) {
    return prompt.formatMessages(messages, variableVals);
  }

  @Override
  public Reply execute(ExecuteParam param) {
    Prompt prompt = param.getPrompt();
    List<Message> messages = param.getMessages();

    PromptDraft promptDraft = prompt.getPromptDraft();
    PromptCommit promptCommit = prompt.getPromptCommit();
    PromptDetail promptDetail = promptDraft == null ? promptCommit.getPromptDetail() : promptDraft.getPromptDetail();
    ModelConfig modelConfig = promptDetail.getModelConfig();

    Long modelId = modelConfig.getModelId();

    Long tenantId = -1L;
    SimpleLargeModelDTO model = largeModelManageMapper.selectLargeModelById(tenantId, modelId);
    if (model == null) {
      throw new BssException("Model not found, modelId=" + modelId);
    }
    LlmConvertor.toModel(modelConfig, model);

    LlmProperties properties = ModelConfigUtil.buildLlmProperties(model);
    LlmClient llmClient = ModelClientUtil.createLlmClient(model.getProtocolType(), properties);

    // 完成多模态文件URI到URL的转换
    mCompleteMultiModalFileURL(messages);

    messages = formatPrompt(prompt, messages, param.getVariableVals());

    List<com.iwhalecloud.bote.llm.client.dto.message.Message> boteMessages = LlmConvertor.toMessage(messages);

    ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder().messages(boteMessages).build();

    ChatCompletionResponse response = llmClient.chatCompletion(chatCompletionRequest);

    return LlmConvertor.toReply(response);
  }

  @Override
  public void mCompleteMultiModalFileURL(List<Message> messages) {
    List<String> fileKeys = extractFileKeys(messages);
    if (fileKeys.isEmpty()) {
      return;
    }

    Map<String, String> urlMap = mGetFileURL(fileKeys);
    fillBackUrls(messages, urlMap);
  }

  private List<String> extractFileKeys(List<Message> messages) {
    List<String> fileKeys = Lists.newArrayList();
    for (Message message : messages) {
      if (message == null || message.getParts() == null || message.getParts().isEmpty()) {
        continue;
      }
      for (ContentPart part : message.getParts()) {
        if (part == null || part.getImageUrl() == null) {
          continue;
        }
        fileKeys.add(part.getImageUrl().getUri());
      }
    }
    return fileKeys;
  }

  private void fillBackUrls(List<Message> messages, Map<String, String> urlMap) {
    for (Message message : messages) {
      if (message == null || message.getParts() == null || message.getParts().isEmpty()) {
        continue;
      }
      for (ContentPart part : message.getParts()) {
        if (part == null || part.getImageUrl() == null) {
          continue;
        }
        part.getImageUrl().setUrl(urlMap.get(part.getImageUrl().getUri()));
      }
    }
  }

  private Map<String, String> mGetFileURL(List<String> keys) {
    Map<String, String> urls = Maps.newHashMap();

    for (String key : keys) {
      String dataUri = buildFileMessageContent(key);
      urls.put(key, dataUri);
    }
    return urls;
  }

  /**
   * 构造文件消息内容
   */
  private String buildFileMessageContent(Object value) {
    // 字符串，可以是文件地址或文件 ID
    if (value instanceof String str) {
      // 文件地址或 data URI
      //noinspection HttpUrlsUsage
      if (str.startsWith("http://") || str.startsWith("https://") || str.startsWith("data:")) {
        return str;
      }
      // 纯数字当作文件 ID
      if (StringUtils.isNumeric(str)) {
        Long fileId = Long.parseLong(str);
        return buildFileMessageContentByFileId(fileId);
      }
      throw new BssException("非法的文件地址: " + value);
    }
    // 数值类型当作文件 ID
    else if (value instanceof Number) {
      Long fileId = ((Number) value).longValue();
      return buildFileMessageContentByFileId(fileId);
    }
    throw new BssException("非法的文件赋值: type=" + value.getClass().getCanonicalName() + ", value=" + value);
  }

  private String buildFileMessageContentByFileId(Long fileId) {
    FileInfoVO fileInfo = fileStoreService.getFileInfoById(fileId);
    if (fileInfo == null) {
      throw new BssException("文件不存在: " + fileId);
    }
    String fileType = fileInfo.getFileType();
    String mimeType = switch (fileType.toLowerCase()) {
      case "png", "apng" -> "image/png";
      case "jpg", "jpeg" -> "image/jpeg";
      case "webp" -> "image/webp";
      case "bmp" -> "image/bmp";
      default -> throw new BssException("不支持的图片格式: " + fileType);
    };
    // 下载文件，构造 data URI 字符串
    byte[] bytes = fileStoreService.downloadFile(fileId);
    if (bytes == null) {
      throw new BssException("文件不存在: " + fileId);
    }
    Assert.isTrue(bytes.length > 0, () -> "文件不存在: " + fileId);
    return "data:" + mimeType + ";base64," + Base64.encodeBase64String(bytes);
  }

  @Override
  public Map<String, Long> mGetPromptIDs(Long spaceID, List<String> promptKeys) {
    Map<String, Long> promptKeyIDMap = new HashMap<>();
    if (promptKeys == null || promptKeys.isEmpty()) {
      return promptKeyIDMap;
    }
    List<Prompt> basics = manageRepo.mGetPromptBasicByPromptKey(spaceID, promptKeys, PromptOptions.withPromptBasicCacheEnable());
    for (Prompt basic : basics) {
      promptKeyIDMap.put(basic.getPromptKey(), basic.getId());
    }
    for (String promptKey : promptKeys) {
      if (!promptKeyIDMap.containsKey(promptKey)) {
        throw new BssException(String.format("prompt key: %s not found", promptKey));
      }
    }
    return promptKeyIDMap;
  }

  @Override
  public Map<PromptKeyVersionPair, String> mParseCommitVersionByPromptKey(Long spaceID, List<PromptKeyVersionPair> pairs) {
    Map<PromptKeyVersionPair, String> resultMap = new HashMap<>();
    List<String> emptyVersionKeys = new ArrayList<>();
    for (PromptKeyVersionPair pair : pairs) {
      resultMap.put(pair, pair.getVersion());
      if (pair.getVersion() == null || pair.getVersion().isEmpty()) {
        emptyVersionKeys.add(pair.getPromptKey());
      }
    }
    if (CollectionUtils.isEmpty(emptyVersionKeys)) {
      return resultMap;
    }

    List<Prompt> basics = manageRepo.mGetPromptBasicByPromptKey(spaceID, emptyVersionKeys, PromptOptions.withPromptBasicCacheEnable());
    for (Prompt basic : basics) {
      String promptKey = basic.getPromptKey();
      PromptBasic promptBasic = basic.getPromptBasic();
      if (promptBasic == null) {
        continue;
      }
      String latestVersion = promptBasic.getLatestVersion();
      if (latestVersion == null || latestVersion.isEmpty()) {
        throw new BssException(String.format("prompt key: %s", promptKey));
      }
      resultMap.put(PromptKeyVersionPair.builder().promptKey(promptKey).build(), latestVersion);
    }

    return resultMap;
  }
}
