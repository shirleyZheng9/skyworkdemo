package com.iwhalecloud.bote.doc.module.person.dto.homepage.query;

import com.iwhalecloud.bote.doc.common.model.PageParams;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 最近访问文件查询参数
 *
 * @author yangran
 * @since 2025-01-06
 */
@Getter
@Setter
@ToString
@Schema(description = "最近访问文件查询参数")
public class RecentFileQueryParams extends PageParams {
  @Schema(description = "用户ID")
  private Long userId;
  @Schema(description = "排序方式：lastOpen-最近打开，lastModified-最近修改")
  private String sortBy;
  @Schema(description = "排序顺序：desc-倒序，asc-正序")
  private String sortOrder;
  @Schema(description = "文件类型过滤")
  private String fileType;
  @Schema(description = "创建者过滤：me-我创建的，others-他人创建的")
  private String creatorFilter;

}
