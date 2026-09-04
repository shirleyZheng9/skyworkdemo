package com.iwhalecloud.bote.dto.base.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 目录查询参数
 *
 * @author auto
 * @since 2024-09-13
 */
@Getter
@Setter
@ToString
public class CatalogQueryParams {
  @Schema(description = "租户ID")
  private Long tenantId;
  @Schema(description = "目录类型")
  private String catalogType;
  @Schema(description = "目录名称")
  private String catalogName;
  @Schema(description = "目录ID")
  private Long catalogId;
  @Schema(description = "机器人ID")
  private Long botId;
  @Schema(description = "企业空间ID")
  protected Long spaceId;
  @Schema(description = "是否是新增操作：T为知识库新增的查询,空或者其他值则不是")
  private String addAction;
  @Schema(description = "ai门户是否为企业，T为企业，F为项目，空则是开发项目")
  private String enterprise;
  @Schema(description = "企业租户id")
  private Long spacetenantId;
}
