package com.iwhalecloud.bote.doc.module.person.dto.homepage.query;

import com.iwhalecloud.bote.doc.common.model.PageParams;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 搜索文档查询参数
 *
 * @author yangran
 * @since 2025-01-06
 */
@Getter
@Setter
@ToString
@Schema(description = "搜索文档查询参数")
public class SearchDocumentQueryParams extends PageParams {
  @Schema(description = "用户ID")
  private Long userId;

  @Schema(description = "搜索关键词")
  private String keyword;

  @Schema(description = "文档类型过滤")
  private String type;

  @Schema(description = "文档库ID过滤")
  private String libraryId;

}
