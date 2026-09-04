package com.iwhalecloud.bote.doc.listener.event;

import com.iwhalecloud.bote.dto.knowledge.KnowledgeRecallResponse;
import com.iwhalecloud.bote.dto.knowledge.ReferenceDocumentDTO;
import java.util.List;

import com.iwhalecloud.bss.litchi.disruptor.DisruptorObject;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文档库文档更新事件消息
 *
 * @author Aiqing
 * @since 2025/8/19
 */
@Getter
@Setter
@ToString
public class DocKnowledgeQaEventMessage implements DisruptorObject {
  /** 问答类型：问答QA：召回RECALL*/
  private String actionType;
  /** 问答记录片段引用*/
  private List<ReferenceDocumentDTO> references;
  /** 问答回答信息*/
  private String text;
  /** 问答id */
  private String chatLogId;
  /** 使用时长*/
  private Long timeSpent;
  /** 机器人 ID */
  private Long botId;
  /** 流式会话id*/
  private String clientId;
  /** 操作人*/
  private Long userId;
  /** 问句 */
  private String question;
  /** 知识库 ID 列表 */
  private List<Long> knowledgeIds;
  /** 租户 ID */
  private Long tenantId;
  /** 召回的返回信息 ID */
  private KnowledgeRecallResponse finalResponse;

}
