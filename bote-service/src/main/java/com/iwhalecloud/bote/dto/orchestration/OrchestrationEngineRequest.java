package com.iwhalecloud.bote.dto.orchestration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iwhalecloud.bote.dto.orchestration.context.SceneOrchestrationContext;
import com.iwhalecloud.bote.dto.scene.SceneChatParamsDTO;
import com.iwhalecloud.bote.llm.client.dto.message.Message;
import com.iwhalecloud.bote.service.orchestration.reply.ReplyHandler;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.lang.Nullable;

/**
 * 场景编排引擎执行请求对象
 *
 * @author bianjp
 * @since 2024-08-29
 */
@Getter
@Setter
@ToString
public class OrchestrationEngineRequest {
  /** 是否调试 */
  private Boolean debug;
  /** 是否调试内部服务（只有运行模式为调试时生效） */
  private Boolean debugInnerService;
  /** 是否记录日志 */
  private Boolean logEnabled;
  /** 租户 ID */
  private Long tenantId;
  /** 机器人 ID */
  private Long botId;
  /** 场景 ID */
  private Long sceneId;
  /** 流程 ID */
  private Long flowId;
  /** 对话标识 */
  private Long conversationId;
  /** 事务 ID (对应一轮对话，一问一答) */
  private Long transactionId;
  /** 上下文 ID */
  private String contextId;
  /** 是否持久化上下文。仅用于对话流，默认为 true */
  private Boolean persistContext;
  /** 用户上传的文件 ID 列表 */
  private List<Long> fileIds;
  /** 用户消息内容 */
  private String messageContent;
  /** 入参，可选 */
  private Map<String, Object> parameters;
  /** 上下文参数 */
  private Map<String, Object> contextParams;
  /** 回复处理器 */
  @JsonIgnore
  private ReplyHandler replyHandler;
  /** 历史消息加载器 */
  @JsonIgnore
  private Supplier<List<Message>> historyMessagesLoader;
  /** 动态生成的 DSL */
  private SceneDslDTO dynamicDsl;
  /** 标记已完成的步骤 */
  private String completedNodeCode;
  /** 标记已完成步骤对应的上下文 ID */
  private String completedContextId;
  /** 计划 ID */
  private Long planId;

  public OrchestrationEngineRequest() {
  }

  public OrchestrationEngineRequest(SceneOrchestrationContext context, Long flowId, @Nullable Map<String, Object> params) {
    if (context.getRequest().isDebugEnabled()) {
      this.debug = context.getRequest().getDebugInnerService();
      this.debugInnerService = context.getRequest().getDebugInnerService();
    }
    this.logEnabled = context.getRequest().getLogEnabled();
    this.tenantId = context.getRequest().getTenantId();
    this.botId = context.getRequest().getBotId();
    this.conversationId = context.getRequest().getConversationId();
    this.transactionId = context.getRequest().getTransactionId();
    this.contextId = context.getRequest().getContextId();
    this.flowId = flowId;
    this.fileIds = context.getRequest().getFileIds();
    this.messageContent = context.getRequest().getMessageContent();
    this.parameters = params;
    this.contextParams = context.getRequest().getContextParams();
    this.replyHandler = context.getReplyHandler();
    this.historyMessagesLoader = context.getRequest().getHistoryMessagesLoader();
    this.planId = context.getRequest().getPlanId();
  }

  public OrchestrationEngineRequest(SceneChatParamsDTO sceneChatParams, @Nullable Long sceneId, @Nullable Long flowId, @Nullable Map<String, Object> params) {
    this.debug = sceneChatParams.getDebug();
    this.debugInnerService = sceneChatParams.getDebugInnerService();
    this.logEnabled = sceneChatParams.getLogEnabled();
    this.tenantId = sceneChatParams.getTenantId();
    this.botId = sceneChatParams.getBotId();
    this.conversationId = sceneChatParams.getConversationId();
    this.transactionId = sceneChatParams.getTransactionId();
    this.contextId = sceneChatParams.getContextId();
    this.sceneId = sceneId;
    this.flowId = flowId;
    this.fileIds = sceneChatParams.getFileIds();
    this.messageContent = sceneChatParams.getMessageContent();
    this.parameters = params;
    this.contextParams = sceneChatParams.getContextParams();
    this.replyHandler = sceneChatParams.getReplyHandler();
    this.historyMessagesLoader = sceneChatParams.getHistoryMessagesLoader();
    this.completedNodeCode = sceneChatParams.getCompletedNodeCode();
    this.completedContextId = sceneChatParams.getCompletedContextId();
    this.planId = sceneChatParams.getPlanId();
    this.dynamicDsl = sceneChatParams.getDynamicDsl();
  }

  /**
   * 是否是调试模式
   */
  @JsonIgnore
  public boolean isDebugEnabled() {
    return Boolean.TRUE.equals(debug);
  }

}
