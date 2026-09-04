package com.iwhalecloud.bote.doc.module.knowledge.entity;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * knowledgeGraph 会话映射实体
 *
 * @author qian.sisheng
 * @since 2026-04-13
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_knowledge_graph_session_rel")
public class KnowledgeGraphSessionRelEntity extends BaseEntity {
  @DiffId
  @Schema(description = "主键")
  private Long id;
  @DiffField(name = "BOTE_SESSION_ID")
  private String boteSessionId;
  @DiffField(name = "KNOWLEDGE_BASE_ID")
  private String knowledgeBaseId;
  @DiffField(name = "KG_SESSION_ID")
  private String kgSessionId;
  @DiffField(name = "TENANT_ID")
  private Long tenantId;
  @DiffField(name = "USER_ID")
  private Long userId;
}
