package com.iwhalecloud.bote.entity.skill;

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
 * 技能：数据源 Entity
 *
 * @author auto
 * @since 2024-09-16
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_data_source")
public class DataSourceEntity extends BaseEntity {
  @DiffId
  @Schema(description = "数据源ID")
  private Long dataSourceId;
  @DiffField(name = "DATASOURCE_CODE")
  @Schema(description = "数据源编码")
  @Size(max = 50, message = "数据源编码超过限定长度50")
  private String dataSourceCode;
  @DiffField(name = "DATASOURCE_NAME")
  @Schema(description = "数据源名称")
  @Size(max = 20, message = "数据源名称超过限定长度20")
  private String dataSourceName;
  @DiffField(name = "DATASOURCE_TYPE")
  @Schema(description = "数据源类型")
  private String dataSourceType;
  @DiffField(name = "CATALOG_ITEM_ID")
  @Schema(description = "目录ID")
  private Long catalogItemId;
  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户ID")
  private Long tenantId;
  @DiffField(name = "DATA_SOURCE_CHANNEL")
  @Schema(description = "数据库渠道来源")
  private String dataSourceChannel;
}
