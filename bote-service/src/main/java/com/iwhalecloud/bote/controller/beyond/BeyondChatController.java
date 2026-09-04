package com.iwhalecloud.bote.controller.beyond;

import com.iwhalecloud.bassc.basiccenter.annotation.IgnoreSession;
import com.iwhalecloud.bote.beyond.BeyondFileHelper;
import com.iwhalecloud.bote.beyond.BeyondSessionHelper;
import com.iwhalecloud.bote.common.annotation.IgnoreSign;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.ChatConsts;
import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.common.sse.SseUtil;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.common.util.TenantIdUtil;
import com.iwhalecloud.bote.dto.beyond.BeyondChatRequest;
import com.iwhalecloud.bote.dto.orchestration.OrchestrationEngineResponse;
import com.iwhalecloud.bote.dto.scene.SceneChatParamsDTO;
import com.iwhalecloud.bote.service.orchestration.reply.handlers.SceneTestReplyHandler;
import com.iwhalecloud.bote.service.scene.ISceneChatService;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 百应会话接口
 *
 * @author bianjp
 * @since 2025-07-17
 */
@RestController
@RequestMapping(BaseConsts.API_PREFIX + "beyond/")
@ConditionalOnBooleanProperty("beyond.enabled")
@Tag(name = "百应会话接口")
@RequiredArgsConstructor
public class BeyondChatController {
  private static final Logger logger = LoggerFactory.getLogger(BeyondChatController.class);

  private final ISceneChatService sceneChatService;
  private final BeyondFileHelper beyondFileHelper;
  private final BeyondSessionHelper beyondSessionHelper;

  @PostMapping(path = "chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  @Operation(summary = "百应会话接口")
  public SseEmitter chat(@RequestBody BeyondChatRequest request, @RequestParam("sceneId") Long sceneId) {
    // 兼容使用 extParam 传递 clientId
    String clientId = StringUtils.isNotEmpty(request.getClientId()) ? request.getClientId() : MapUtils.getString(request.getExtParam(), "clientId");
    return SseUtil.createSseEmitter(clientId, false, true, sseEmitter -> {
      try {
        Long tenantId = TenantIdUtil.getTenantId();
        Assert.notNull(tenantId, "租户 ID 不能为空");
        Assert.notNull(sceneId, "智能体 ID 不能为空");
        Assert.isTrue(StringUtils.isNotEmpty(request.getChatContent()) || CollectionUtils.isNotEmpty(request.getFiles()), "消息内容和文件列表不能同时为空");
        SceneChatParamsDTO sceneChatParams = new SceneChatParamsDTO();
        sceneChatParams.setTenantId(tenantId);
        sceneChatParams.setSceneId(sceneId);
        sceneChatParams.setConversationId(ChatConsts.BEYOND_SESSION_ID);
        sceneChatParams.setTransactionId(IDUtils.nextId());
        sceneChatParams.setContextId(request.getSessionId());
        sceneChatParams.setClientId(clientId);
        if (request.getExtParam() == null) {
          sceneChatParams.setContextParams(new HashMap<>());
        }
        else {
          sceneChatParams.setContextParams(request.getExtParam());
        }
        sceneChatParams.setMessageContent(request.getChatContent());
        // 上传文件
        sceneChatParams.setFileIds(beyondFileHelper.uploadFiles(tenantId, request.getFiles()));
        // 记录sessionId
        beyondSessionHelper.saveBeyondSession(request.getSessionId(), tenantId);
        // sessionId放入扩展参数中
        sceneChatParams.getContextParams().put("beyondSessionId", request.getSessionId());
        sceneChatParams.setReplyHandler(new SceneTestReplyHandler(sseEmitter, clientId));
        sceneChatParams.setHistoryMessagesLoader(() -> ListUtils.emptyIfNull(request.getHistories()));
        OrchestrationEngineResponse response = sceneChatService.run(sceneChatParams);
        if (Boolean.TRUE.equals(response.getSuccess())) {
          SseUtil.sendText(sseEmitter, ChatMessageType.DONE, ChatConsts.COMPLETIONS_DONE);
        }
        else {
          SseUtil.sendJson(sseEmitter, ChatMessageType.ERROR, StringUtils.defaultIfEmpty(response.getFailMsg(), "调用智能体失败"));
        }
      }
      catch (Exception e) {
        logger.warn("Failed to process beyond chat", e);
        SseUtil.sendJson(sseEmitter, ChatMessageType.ERROR, ExpUtil.getMsg(e));
      }
      finally {
        SseUtil.completeQuietly(sseEmitter);
      }
    });
  }

  @GetMapping(path = "bindSessionSsoToken")
  @Operation(summary = "百应会话绑定鲸加token接口")
  @IgnoreSession
  @IgnoreSign
  public boolean bindSessionSsoToken(@RequestParam("sessionId") String sessionId, @RequestParam("code") String code) {
    return beyondSessionHelper.bindSessionSsoToken(sessionId, code);
  }
}
