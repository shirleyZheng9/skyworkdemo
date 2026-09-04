package com.iwhalecloud.bote.loop.prompt.domain.repo.dto;

import com.iwhalecloud.bote.dto.base.query.PagingQueryParams;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 列表查询Prompt参数
 */
@Getter
@Setter
@ToString
public class ListPromptParam extends PagingQueryParams {
  @Schema(description = "空间ID")
  private Long spaceId;
  @Schema(description = "关键词")
  private String keyWord;
  @Schema(description = "创建者列表")
  private List<String> createdBys;
  @Schema(description = "用户ID")
  private String userId;
  @Schema(description = "仅查询已提交")
  private Boolean committedOnly;
  @Schema(description = "排序字段")
  private Integer orderBy;
  @Schema(description = "是否升序")
  private Boolean asc;
  @Schema(description = "目录ID")
  private Long catalogItemId;
  @Schema(description = "Prompt类型")
  private String promptType;
}
