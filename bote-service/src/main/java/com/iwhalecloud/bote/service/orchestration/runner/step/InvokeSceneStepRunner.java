package com.iwhalecloud.bote.service.orchestration.runner.step;

import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.common.util.SceneContextUtil;
import com.iwhalecloud.bote.dto.chat.ReplyDTO;
import com.iwhalecloud.bote.dto.orchestration.OrchestrationEngineResponse;
import com.iwhalecloud.bote.dto.orchestration.log.OrchestrationStepRunLog;
import com.iwhalecloud.bote.dto.orchestration.step.InvokeSceneStep;
import com.iwhalecloud.bote.dto.scene.SceneChatParamsDTO;
import com.iwhalecloud.bote.service.orchestration.reply.ReplyHandler;
import com.iwhalecloud.bote.service.orchestration.reply.handlers.SubSceneReplyHandler;
import com.iwhalecloud.bote.service.orchestration.runner.AbstractStepRunner;
import com.iwhalecloud.bote.service.scene.ISceneChatService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 调用智能体步骤执行器
 * <p>将 ISceneChatService#run 封装为工具调用：使用独立 contextId 调用子智能体，收集回复文本作为工具出参返回给规划模型。</p>
 *
 * @author chen.linfa
 * @since 2026-03-16
 */
public class InvokeSceneStepRunner extends AbstractStepRunner<InvokeSceneStep> {

  private static final Logger logger = LoggerFactory.getLogger(InvokeSceneStepRunner.class);
  private static final ISceneChatService sceneChatService = SpringUtil.getBean(ISceneChatService.class);

  @Override
  @Nullable
  public Object runAsTool(SceneChatParamsDTO sceneChatParams, InvokeSceneStep step, String toolCallId,
                          @Nullable Map<String, Object> toolArguments, Optional<OrchestrationStepRunLog> log) {
    String message = toolArguments != null ? StringUtils.trimToEmpty((String) toolArguments.get("message")) : "";
    Assert.notNull(step.getSceneId(), "调用智能体时 sceneId 不能为空");
    String contextId = SceneContextUtil.newContextId();
    Map<String, Object> input = new LinkedHashMap<>();
    input.put("sceneId", step.getSceneId());
    input.put("sceneName", step.getSceneName());
    input.put("params", toolArguments);
    input.put("contextId", contextId);
    log.ifPresent(l -> l.setInput(input));
    log.ifPresent(l -> l.addLog("调用子智能体: sceneId=%s, sceneName=%s, contextId=%s", step.getSceneId(), step.getSceneName(), contextId));

    SceneChatParamsDTO subParams = new SceneChatParamsDTO();
    subParams.setTenantId(sceneChatParams.getTenantId());
    subParams.setBotId(sceneChatParams.getBotId());
    subParams.setSceneId(step.getSceneId());
    subParams.setContextId(contextId);
    subParams.setConversationId(sceneChatParams.getConversationId());
    subParams.setMessageContent(message);
    ReplyHandler mainHandler = sceneChatParams.getReplyHandler();
    SubSceneReplyHandler subHandler = new SubSceneReplyHandler(mainHandler);
    subParams.setReplyHandler(subHandler);
    subParams.setClientId(sceneChatParams.getClientId());
    subParams.setDebug(sceneChatParams.getDebug());
    subParams.setLogEnabled(sceneChatParams.getLogEnabled());

    OrchestrationEngineResponse response = sceneChatService.run(subParams);

    log.ifPresent(l -> l.addLog("子智能体执行完成: success=%s, timeSpent=%s", response.getSuccess(), response.getTimeSpent()));
    log.ifPresent(l -> {
      if (Boolean.TRUE.equals(sceneChatParams.getDebug())) {
        l.setInnerServiceLogs(response.getStepLogs());
      }
      else if (response.getLogId() != null) {
        l.setInnerServiceLogId(response.getLogId());
      }
    });

    if (!Boolean.TRUE.equals(response.getSuccess())) {
      String failMsg = StringUtils.isNotBlank(response.getFailMsg()) ? response.getFailMsg() : "子智能体执行失败";
      log.ifPresent(l -> l.fail(new BssException(failMsg)));
      return "【" + step.getSceneName() + "】执行失败：" + failMsg;
    }

    List<ReplyDTO> replies = response.getReplies();
    String sceneName = StringUtils.defaultIfEmpty(step.getSceneName(), "子智能体");
    String prefix = "【以下为子智能体「" + sceneName + "」的返回结果，请严格据此回答用户】\n\n";

    if (CollectionUtils.isEmpty(replies)) {
      if (logger.isDebugEnabled()) {
        logger.debug("InvokeSceneStep: sceneId={}, sceneName={}, replies is null or empty, check sub-scene replyHandler", step.getSceneId(), sceneName);
      }
      String result = prefix + "（该智能体未返回文本内容，请根据已有信息回答或提示用户稍后重试。）";
      log.ifPresent(l -> l.succeed(result));
      return result;
    }

    // 如果回复中含有页面或回复节点文本，不再交给大模型总结
    OrchestrationEngineResponse replyResponse = handleReplies(log, replies, mainHandler, response);
    if (replyResponse != null) {
      return replyResponse;
    }

    // 无页面时：拼成字符串交给大模型总结
    String body = getString(replies);
    String result = StringUtils.isNotBlank(body) ? (prefix + body) : (prefix + "（该智能体未返回文本内容。）");
    log.ifPresent(l -> l.succeed(result));
    return result;
  }

  /**
   * 处理回复内容
   */
  @Nullable
  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  private OrchestrationEngineResponse handleReplies(Optional<OrchestrationStepRunLog> log, List<ReplyDTO> replies,
    ReplyHandler mainHandler, OrchestrationEngineResponse response) {
    // 如果回复中含有页面或回复节点文本，不再交给大模型总结
    boolean isReply = replies.stream().anyMatch(
      r -> r != null && (ChatMessageType.PAGE.equals(r.getType()) || (ChatMessageType.TEXT.equals(r.getType())
        && StringUtils.isNotBlank(r.getCode()))));

    if (isReply) {
      forwardReplies(replies, mainHandler);
      log.ifPresent(l -> l.succeed(replies));
      return response;
    }
    return null;
  }

  /**
   * 将回复内容拼接成字符串
   */
  private String getString(List<ReplyDTO> replies) {
    StringBuilder sb = new StringBuilder();
    for (ReplyDTO r : replies) {
      if (r == null) {
        continue;
      }
      if (StringUtils.isNotBlank(r.getReasoning())) {
        sb.append(r.getReasoning()).append("\n\n");
      }
      if (StringUtils.isNotBlank(r.getText())) {
        sb.append(r.getText()).append("\n\n");
      }
    }
    return sb.toString().trim();
  }

  /**
   * 将子智能体的回复直接转发给主 replyHandler
   */
  private void forwardReplies(List<ReplyDTO> replies, ReplyHandler replyHandler) {
    for (ReplyDTO r : replies) {
      if (r == null) {
        continue;
      }
      if (StringUtils.isNotBlank(r.getText()) && StringUtils.isNotEmpty(r.getCode())) {
        replyHandler.reply(ChatMessageType.TEXT, r.getText(), r.getCode(), r.getName());
      }
      if (MapUtils.isNotEmpty(r.getPage())) {
        replyHandler.reply(ChatMessageType.PAGE, r.getPage(), r.getCode(), r.getName());
      }
    }
  }
}
