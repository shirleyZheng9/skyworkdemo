package com.iwhalecloud.bote.dto.knowledge.query;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文件编辑参数
 *
 * @author auto
 * @since 2025-02-20
 */
@Getter
@Setter
@ToString
@Schema(description = "文件编辑参数")
public class UpdateFileParams {
  @Schema(description = "文件信息ID", requiredMode = RequiredMode.REQUIRED)
  private Long fileInfoId;
  @Schema(description = "租户 ID")
  private Long tenantId;
  @Schema(description = "目录 ID")
  private Long catalogItemId;
  @Schema(description = "业务类型")
  private String busiType;
  @Schema(description = "文件描述")
  private String fileDesc;
  @Schema(description = "业务子类型")
  private String busiSubType;
  @Schema(description = "文件名称")
  private String fileName;
  @Schema(description = "组件的入参定义")
  private String reqJson;
}
