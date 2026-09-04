package com.iwhalecloud.bote.dto.knowledge.access.query;

import com.iwhalecloud.bote.dto.base.query.PagingQueryParams;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 知识库分页查询参数
 *
 * @author lxs
 * @since 2025/7/12
 */
@Getter
@Setter
@ToString(callSuper = true)
public class KnowledgeQueryParams extends PagingQueryParams {

  @Schema(description = "知识库类型")
  private String knowledgeType;

  @Schema(description = "目录ID")
  private Long catalogId;

  @Schema(description = "知识库名称。模糊搜索")
  private String knowledgeName;

}
