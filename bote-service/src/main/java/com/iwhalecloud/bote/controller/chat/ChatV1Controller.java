package com.iwhalecloud.bote.controller.chat;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.common.consts.ChatConsts;
import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.sse.SseUtil;
import com.iwhalecloud.bote.common.util.ChatContextUtil;
import com.iwhalecloud.bote.common.util.CustomizedSensitiveWordUtil;
import com.iwhalecloud.bote.common.util.OcrUtil;
import com.iwhalecloud.bote.common.util.SensitiveWordUtil;
import com.iwhalecloud.bote.dto.asr.VideoProviderInfoDTO;
import com.iwhalecloud.bote.dto.chat.ChatRequestDTO;
import com.iwhalecloud.bote.dto.chat.ChatRequestMessageDTO;
import com.iwhalecloud.bote.dto.chat.SceneIntentResultDTO;
import com.iwhalecloud.bote.dto.chat.SearchResultDTO;
import com.iwhalecloud.bote.dto.chat.query.SceneIntentParams;
import com.iwhalecloud.bote.dto.chat.query.SearchQueryParams;
import com.iwhalecloud.bote.dto.planning.query.PlanParams;
import com.iwhalecloud.bote.intent.ISceneIntentService;
import com.iwhalecloud.bote.llm.client.util.LlmTraceUtil;
import com.iwhalecloud.bote.observability.LangfuseTracingService;
import com.iwhalecloud.bote.service.asr.realtime.RealtimeAsrFactory;
import com.iwhalecloud.bote.service.chat.IChatService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.cache.refresh.IRefreshCacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 对话控制器
 *
 * @author Admin
 */
@RestController
@RequestMapping(BaseConsts.API_PREFIX + "chat")
@RequiredArgsConstructor
@Tag(name = "对话：人机交互")
public class ChatV1Controller {
  private final Logger logger = LoggerFactory.getLogger(ChatV1Controller.class);

  private final IChatService chatService;
  private final ISceneIntentService sceneIntentService;
  private final IRefreshCacheService refreshCacheService;
  private final RealtimeAsrFactory realtimeAsrFactory;
  private final LangfuseTracingService langfuseTracingService;

  @Operation(summary = "租户欢迎页意图识别应用")
  @PostMapping("/recognize")
  public ResultVO<SceneIntentResultDTO> recognize(@RequestBody SceneIntentParams params) {
    Assert.notNull(params.getTenantId(), "租户 ID 不能为空");
    Assert.hasText(params.getMessage(), "内容不能为空");
    SceneIntentResultDTO result = sceneIntentService.recognizeBot(params.getTenantId(), params.getMessage());
    return ResultVO.success(result);
  }

  @Operation(summary = "对话补全", description = "对话补全")
  @PostMapping(path = "/completions", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter completions(@RequestBody ChatRequestDTO request) {
    return SseUtil.createSseEmitter(request.getClientId(), true, false, sseEmitter -> {
      try {
        validateRequest(request);
      }
      catch (IllegalArgumentException e) {
        SseUtil.sendJson(sseEmitter, ChatMessageType.ERROR, e.getMessage());
        SseUtil.sendText(sseEmitter, ChatMessageType.DONE, ChatConsts.COMPLETIONS_DONE);
        SseUtil.completeQuietly(sseEmitter);
        return;
      }
      ChatContextUtil.setChatSessionId(request.getSessionId() + "");
      String traceId = langfuseTracingService.beginAgentTurn(
        request.getTraceId(), "chat", request.getMessage(), request.getSessionId());
      request.setTraceId(traceId);
      try {
        chatService.completions(request, sseEmitter);
      }
      finally {
        langfuseTracingService.endAgentTurn(null);
        ChatContextUtil.clear();
        LlmTraceUtil.clearTraceId();
      }
    });
  }

  /**
   * 校验请求对象
   */
  private void validateRequest(ChatRequestDTO request) {
    ChatRequestMessageDTO message = request.getMessage();
    Assert.notNull(message, "消息不能为空");
    PlanParams planParams = request.getPlanParams();
    if (planParams != null && planParams.getPlanId() == null && CollectionUtils.isEmpty(planParams.getSteps()) && !planParams.recognizePlan()) {
      planParams = null;
      request.setPlanParams(null);
    }
    if (planParams == null) {
      // 消息内容、文件、场景不能同时为空
      Assert.isTrue(StringUtils.isNotBlank(message.getContent()) || CollectionUtils.isNotEmpty(message.getFileIds()) || request.getSceneId() != null,
        "请输入消息");
    }
    Assert.isTrue(!SensitiveWordUtil.isSensitive(message.getContent()), SystemParameter.SENSITIVE_MSG_REPLY::getValueFromDb);
    CustomizedSensitiveWordUtil.checkSensitive(message.getContent(), request.getTenantId(), BaseConsts.SECURITY_TYPE_USER_INPUT);
    message.setType(StringUtils.defaultIfEmpty(message.getType(), ChatMessageType.INPUT.getCode()));
  }

  @Operation(summary = "主动中断对话")
  @GetMapping("/cancel")
  public ResultVO<Void> cancel(@RequestParam("clientId") String clientId) {
    Assert.hasText(clientId, "客户端 ID 不能为空");
    refreshCacheService.refresh(CacheConsts.CACHE_NAME_SSE_EMITTER, clientId);
    return ResultVO.success();
  }

  @Operation(summary = "语音识别")
  @PostMapping("/asr")
  public ResultVO<String> asr(@RequestParam("file") MultipartFile file) {
    String originalFilename = file.getOriginalFilename();
    Assert.hasLength(originalFilename, "文件名不能为空");
    Path tempDir = null;
    try {
      tempDir = Files.createTempDirectory("bote-asr-");
      File tempFile = new File(tempDir.toFile(), originalFilename);
      try (InputStream inputStream = file.getInputStream()) {
        FileUtils.copyInputStreamToFile(inputStream, tempFile);
      }
      return ResultVO.success(OcrUtil.asr(tempFile));
    }
    catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Failed to asr. error={}", e.getMessage(), e);
      }
      return ResultVO.fail(e.getMessage());
    }
    finally {
      if (tempDir != null) {
        FileUtils.deleteQuietly(tempDir.toFile());
      }
    }
  }

  @Operation(summary = "AI 门户通用搜索")
  @PostMapping("search")
  public ResultVO<SearchResultDTO> search(@RequestBody SearchQueryParams params) {
    return chatService.search(params);
  }

  @Operation(summary = "获取当前语音识别信息")
  @GetMapping("getVoiceProvider")
  public ResultVO<VideoProviderInfoDTO> getProvider() {
    return ResultVO.success(realtimeAsrFactory.getProviderInfo());
  }
}
