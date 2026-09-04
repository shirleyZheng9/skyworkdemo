package com.iwhalecloud.bote.doc.module.knowledge.entity;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 知识库查询记录
 *
 * @author bianjp
 * @since 2024-09-28
 */
@Getter
@Setter
@ToString
public class KnowledgeQueryRecordEntity {
  /** 查询记录 ID */
  private Long id;
  /** 知识库 ID */
  private Long knowledgeId;
  /** 查询来源 */
  private String querySource;
  /** 查询内容 */
  private String content;
  /** 租户 ID */
  private Long tenantId;
  /** 创建人 */
  private Long creatorId;
  /** 创建时间 */
  private Date createdTime;

}

