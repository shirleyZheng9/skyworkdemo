package com.iwhalecloud.bote.dto.base;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 操作日志
 *
 * @author chen.linfa
 * @since 2024-10-26
 */
@Getter
@Setter
@ToString
@Schema(description = "操作日志")
public class OperLogDTO {
  @Schema(description = "操作日志 ID")
  private Long logId;
  @Schema(description = "操作对象 ID")
  private Long objId;
  @Schema(description = "操作对象 JSON")
  private String objDesc;
  @Schema(description = "操作内容")
  private String operContent;
  @Schema(description = "操作类型", example = "A/M/D")
  private String operType;
  @Schema(description = "租户 ID")
  private Long tenantId;
  @Schema(description = "操作时间")
  private Date updatedTime;
  @Schema(description = "操作人 ID")
  private Long updatorId;
  @Schema(description = "操作人名称")
  private String updatorName;
}
