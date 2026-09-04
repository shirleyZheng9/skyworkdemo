package com.iwhalecloud.bote.doc.module.knowledge.dto.weknora.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.doc.common.model.TenantBaseRO;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * WeKnora 知识检索请求
 *
 * @author huangyunming
 * @since 2026-03-31
 */
@Getter
@Setter
@Builder
public class WeKnoraSearchRequest extends TenantBaseRO {

  /** 查询内容 */
  @JsonProperty("query")
  private String query;

  /** 单知识库 ID（与 knowledgeBaseIds / knowledgeIds 三选一） */
  @JsonProperty("knowledge_base_id")
  private String knowledgeBaseId;

  /** 多知识库 ID 列表（与 knowledgeBaseId / knowledgeIds 三选一） */
  @JsonProperty("knowledge_base_ids")
  private List<String> knowledgeBaseIds;

  /** 指定文件 ID 列表（与 knowledgeBaseId / knowledgeBaseIds 三选一） */
  @JsonProperty("knowledge_ids")
  private List<String> knowledgeIds;
}
