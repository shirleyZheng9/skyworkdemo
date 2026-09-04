package com.iwhalecloud.bote.dto.knowledge.access.query;

import com.iwhalecloud.bote.dto.base.query.PagingQueryParams;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 知识库文档分页查询参数
 *
 * @author lxs
 * @since 2025/7/12
 */
@Getter
@Setter
@ToString(callSuper = true)
public class KnowledgeFileQueryParams extends PagingQueryParams {

  @Schema(description = "知识库类型")
  private String knowledgeType;

  @Schema(description = "知识库ID")
  private String knowledgeId;

  @Schema(description = "文档名称。模糊搜索")
  private String docName;

}
