package com.iwhalecloud.bote.loop.client.prompt.manage.dto;

import com.iwhalecloud.bote.dto.base.query.PagingQueryParams;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.PromptType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 列表Prompt请求DTO
 */
@Getter
@Setter
@ToString
public class ListPromptRequest extends PagingQueryParams {
  @Schema(description = "租户 ID")
  private Long tenantId;
  @Schema(description = "关键词")
  private String keyWord;
  @Schema(description = "目录ID")
  private Long catalogItemId;
  @Schema(description = "创建人")
  private List<String> createdBys;
  @Schema(description = "仅查询已提交")
  private Boolean committedOnly;
  @Schema(description = "排序字段")
  private ListPromptOrderBy orderBy;
  @Schema(description = "是否正序")
  private Boolean asc;
  @Schema(description = "Prompt类型")
  private PromptType promptType;
}
