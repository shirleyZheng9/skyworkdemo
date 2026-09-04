package com.iwhalecloud.bote.doc.module.knowledge.dto.knowledgegraph.rsp;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

/**
 * /api/list_task 返回的任务项
 *
 * @author qian.sisheng
 * @since 2026-04-13
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class KnowledgeGraphResponse {
  /** taks_id */
  @JsonAlias({"taskId", "taks_id"})
  private String taskId;
  /** 数据库名称 */
  private String database;
  /** 任务状态 */
  private String status;

}
