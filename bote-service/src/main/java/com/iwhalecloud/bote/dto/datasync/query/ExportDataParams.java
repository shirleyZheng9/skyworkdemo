package com.iwhalecloud.bote.dto.datasync.query;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 导出数据入参
 *
 * @author chen.linfa
 * @since 2024-10-21
 */
@Getter
@Setter
@ToString
public class ExportDataParams {
  @Schema(description = "租户 ID")
  private Long tenantId;

  @Schema(description = "是否全量")
  private Boolean syncAll;

  @Schema(description = "自定义方式，是否导出关联数据")
  private Boolean relatable;

  @Schema(description = "是否备份数据")
  private Boolean backUp;

  @Schema(description = "自定义方式，模块主表的主键值串")
  private Map<String, String> codeAndIds;

  @Schema(description = "导出文件名称，不带后缀（可选，不指定则自动生成）")
  private String exportFileName;

}
