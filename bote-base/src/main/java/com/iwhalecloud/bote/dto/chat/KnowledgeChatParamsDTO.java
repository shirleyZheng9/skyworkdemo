package com.iwhalecloud.bote.dto.chat;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.iwhalecloud.bote.common.consts.ThinkingStrategy;
import com.iwhalecloud.bote.dto.knowledge.ResourceExtItem;
import com.iwhalecloud.bote.dto.knowledge.SimpleKnowledgeDTO;
import com.iwhalecloud.bote.llm.client.dto.CustomModelConfig;
import com.iwhalecloud.bote.llm.client.dto.message.Message;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.lang.Nullable;

/**
 * 知识问答参数
 *
 * @author bianjp
 * @since 2025-03-13
 */
@Getter
@Setter
@ToString
@Builder
@JsonInclude(Include.NON_EMPTY)
public class KnowledgeChatParamsDTO {
  /** 租户 ID */
  private Long tenantId;
  /** 机器人 ID */
  @Nullable
  private Long botId;
  /** 模型 ID */
  @Nullable
  private Long modelId;
  /** 模型信息 */
  @Nullable
  private Map<String, Object> modelInfo;
  /** 知识库 ID 列表 */
  private List<Long> knowledgeIds;
  /** 知识库文档 ID 列表 */
  private List<Long> documentIds;
  /** 知识库主题id列表 */
  private List<String> topicIds;
  /** docchain对应文档id列表 */
  private List<String> docIds;
  /** 知识库列表 */
  private List<SimpleKnowledgeDTO> knowledgeList;
  /**
   * WeKnora 知识库 ID 列表（编排等场景可直接传 WeKnora 侧 ID，无需 {@link #knowledgeList} / knowledgeTypeExt）
   */
  @Nullable
  private List<String> extKnowledgeIds;
  /** 知识库类型 */
  private String knowledgeType;
  /** 问句 */
  private String question;
  /** 历史消息 */
  @Nullable
  private List<Message> history;
  /** 提示词模板 */
  @Nullable
  private String promptTemplate;
  /** 是否返回参考文档 */
  private boolean withReferences;
  /** 是否开启追问 */
  private boolean withQuestions;
  /** 是否开启日志 */
  private boolean withLog;
  /** 推理策略 */
  private ThinkingStrategy thinkingStrategy;
  /** 文档列表 (外系统接入 知识中台使用)*/
  private List<ResourceExtItem> resourceItems;
  /** 自定义模型配置 */
  private CustomModelConfig customModelConfig;

  /** 是否提前记录 */
  private boolean advanceRecord;
  /** 流式会话id*/
  private String clientId;

  /**
   * 编排/场景会话 ID；作为 WeKnora 映射表中的博特会话标识（与 {@link #clientId} 二选一优先使用本字段）。
   * 为 {@code null} 或 {@code -1} 时不写入 {@code bt_weknora_chat_session}（无 {@link #clientId} 时每次在 WeKnora 新建会话）。
   */
  @Nullable
  private String conversationId;

  /** 操作人*/
  private Long userId;

  private Long spaceId;
  /** 虚拟租户ID */
  private Long virtualTenantId;
}
