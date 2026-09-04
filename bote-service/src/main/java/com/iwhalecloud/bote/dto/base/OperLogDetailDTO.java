package com.iwhalecloud.bote.dto.base;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 模块操作日志详情
 *
 * @author chen.linfa
 * @since 2024-10-26
 */
@Getter
@Setter
@ToString
@Schema(description = "模块操作日志")
public class OperLogDetailDTO {
  @Schema(description = "操作日志详情 ID")
  private Long detailId;
  @Schema(description = "操作日志 ID")
  private Long logId;
  @Schema(description = "操作类型", example = "A/M/D")
  private String operType;
  @Schema(description = "操作表编码")
  private String operTableName;
  @Schema(description = "操作字段名称")
  private String operFieldName;
  @Schema(description = "操作对象id")
  private String objId;
  @Schema(description = "旧值")
  private String oldValue;
  @Schema(description = "新值")
  private String newValue;
}
