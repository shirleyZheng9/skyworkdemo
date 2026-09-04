package com.iwhalecloud.bote.entity.base;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 环境变量值 Entity
 *
 * @author qian.sisheng
 * @since 2025-11-03
 */

@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_env_variable_val")
public class EnvVariableValEntity extends BaseEntity {

  @DiffId
  @Schema(description = "环境变量值ID")
  private Long variableValId;

  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户ID")
  private Long tenantId;

  @DiffField(name = "VARIABLE_ID", parent = true)
  @Schema(description = "环境变量ID")
  private Long variableId;

  @DiffField(name = "ENV_CODE")
  @Schema(description = "环境编码")
  private String envCode;

  @DiffField(name = "VARIABLE_VAL")
  @Schema(description = "变量值")
  private String variableVal;
}
