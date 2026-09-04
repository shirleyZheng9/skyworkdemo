package com.iwhalecloud.bote.dto.base.query;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文件查询参数
 *
 * @author auto
 * @since 2024-09-24
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "文件查询参数")
public class FileInfoQueryParams extends PagingQueryParams {
  @Schema(description = "模糊查询")
  private String searchContent;
  @Schema(description = "租户ID")
  private Long tenantId;
  @Schema(description = "按创建时间排序：desc降序 asc升序")
  private String sort;
  @Schema(description = "是否查询全部：T:是 F:否")
  private String isAll;
  @Schema(description = "目录ID")
  private Long catalogItemId;
  @Schema(description = "文件ID列表")
  private List<Long> fileIds;
  @Schema(description = "分类")
  private String busiType;
  @Schema(description = "文件子类型")
  private String busiSubType;
  @Schema(description = "文档类型")
  private String documentType;
  @Schema(description = "文件类型列表")
  private List<String> fileTypes;
  @Schema(description = "文件信息ID列表")
  private List<Long> fileInfoIds;
}
