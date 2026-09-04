package com.iwhalecloud.bote.entity.skill;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 技能：数据源实例 Entity
 *
 * @author auto
 * @since 2024-09-16
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_data_source_inst")
public class DataSourceInstEntity extends BaseEntity {
  @DiffId
  @Schema(description = "数据源实例ID")
  private Long dataSourceInstId;
  @DiffField(required = false, name = "DATA_SOURCE_ID", parent = true)
  @Schema(description = "数据源ID")
  private Long dataSourceId;
  @DiffField(name = "ENV_CODE")
  @Schema(description = "环境编码：dev测试：test生产：prod")
  private String envCode;
  @DiffField(name = "URL")
  @Schema(description = "环境地址")
  private String url;
  @DiffField(name = "USER_NAME")
  @Schema(description = "用户名")
  private String userName;
  @DiffField(name = "PASSWORD")
  @Schema(description = "密码")
  private String password;
  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户ID")
  private Long tenantId;
}
