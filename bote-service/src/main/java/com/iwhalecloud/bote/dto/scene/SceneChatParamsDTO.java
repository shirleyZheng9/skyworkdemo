package com.iwhalecloud.bote.dto.scene;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iwhalecloud.bote.dto.orchestration.OrchestrationEngineRequest;
import com.iwhalecloud.bote.dto.orchestration.SceneDslDTO;
import com.iwhalecloud.bote.llm.client.dto.CustomModelConfig;
import com.iwhalecloud.bote.llm.client.dto.message.Message;
import com.iwhalecloud.bote.service.chat.context.ChatContext;
import com.iwhalecloud.bote.service.orchestration.reply.ReplyHandler;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 场景会话参数
 *
 * @author bianjp
 * @since 2024-08-06
 */
@Getter
@Setter
@ToString
public class SceneChatParamsDTO {
  /** 是否调试 */
  private Boolean debug;
  /** 是否调试内部服务(子流程) */
  private Boolean debugInnerService;
  /** 是否记录日志 */
  @Hidden
  @JsonIgnore
  private Boolean logEnabled;
  /** 租户 ID */
  private Long tenantId;
  /** 机器人 ID */
  private Long botId;
  /** 场景标识 */
  private Long sceneId;
  /** 模型 ID */
  private Long modelId;
  /** 流程 ID */
  private Long flowId;
  /** 对话标识 */
  private Long conversationId;
  /** 事务 ID (对应一轮对话，一问一答) */
  private Long transactionId;
  /** 场景会话标识，可能为空，为空时使用 conversationId 作为会话标识 */
  private String contextId;
  /** 场景入参 */
  private Map<String, Object> params;
  /** 上下文参数 */
  private Map<String, Object> contextParams;
  /** 工具调用标识。不为空时表示 messageContent 是工具调用的结果 */
  private String toolCallId;
  /** 用户上传的文件 ID 列表 */
  private List<Long> fileIds;
  /** 用户消息内容 */
  private String messageContent;
  /** 客户端 ID(流式接口中断请求使用) */
  private String clientId;
  /** 自定义模型配置 */
  private CustomModelConfig customModelConfig;
  /** 是否开启长期记忆 */
  private String longTermMemoryEnabled;

  /** 主场景会话标识，可能为空 */
  private String mainContextId;
  /** 标记已完成的步骤 */
  private String completedNodeCode;
  /** 标记已完成步骤对应的上下文 ID */
  private String completedContextId;

  /** 计划 ID */
  private Long planId;
  /** 动态生成的 DSL */
  private SceneDslDTO dynamicDsl;

  @Schema(description = "历史消息列表。仅用于场景/工作流的调试接口")
  private List<Message> historyMessages;
  @Schema(description = "回复处理器", hidden = true)
  @JsonIgnore
  private ReplyHandler replyHandler;
  @Schema(description = "历史消息加载器", hidden = true)
  @JsonIgnore
  private Supplier<List<Message>> historyMessagesLoader;

  @Schema(description = "会话上下文", hidden = true)
  @JsonIgnore
  private ChatContext chatContext;

  public SceneChatParamsDTO() {
  }

  /**
   * 构造工作流的大模型节点调用技能使用的场景会话参数对象
   */
  public SceneChatParamsDTO(OrchestrationEngineRequest request) {
    this.debug = request.getDebug();
    this.debugInnerService = request.getDebugInnerService();
    this.logEnabled = request.getLogEnabled();
    this.tenantId = request.getTenantId();
    this.botId = request.getBotId();
    this.conversationId = request.getConversationId();
    this.transactionId = request.getTransactionId();
    this.contextId = request.getContextId();
    this.fileIds = request.getFileIds();
    this.messageContent = request.getMessageContent();
    this.contextParams = request.getContextParams();
    this.historyMessagesLoader = request.getHistoryMessagesLoader();
    this.planId = request.getPlanId();
    // 不拷贝 replyHandler, 工作流的大模型节点不支持调用生成回复的技能
  }

  /**
   * 是否是通用智能体
   */
  @JsonIgnore
  public boolean isGeneralAgent() {
    return chatContext != null && Boolean.TRUE.equals(chatContext.getIsClaw());
  }
}
