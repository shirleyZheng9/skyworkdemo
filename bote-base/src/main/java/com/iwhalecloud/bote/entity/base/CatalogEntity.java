package com.iwhalecloud.bote.entity.base;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import jakarta.validation.constraints.Size;

/**
 * 目录 Entity
 *
 * @author auto
 * @since 2024-09-13
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_catalog")
public class CatalogEntity extends BaseEntity {
  @DiffId
  @Schema(description = "主键")
  private Long catalogId;
  @DiffField(name = "PAR_CATALOG_ID")
  @Schema(description = "父目录 ID")
  private Long parCatalogId;
  @DiffField(name = "CATALOG_NAME")
  @Schema(description = "目录名称")
  @Size(max = 20, message = "目录名称超过限定长度20")
  private String catalogName;
  @DiffField(name = "CATALOG_TYPE")
  @Schema(description = "目录类型")
  private String catalogType;
  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户 ID")
  private Long tenantId;
  @DiffField(name = "CATALOG_PATH")
  @Schema(description = "目录路径，逗号分割。由后端计算")
  private String catalogPath;
  @DiffField(name = "BOT_ID")
  @Schema(description = "机器人ID")
  private Long botId;
  @Schema(description = "空间 ID")
  private Long spaceId;
}
