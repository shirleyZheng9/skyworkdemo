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
 * 环境变量 Entity
 *
 * @author qian.sisheng
 * @since 2025-11-03
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_env_variable")
public class EnvVariableEntity extends BaseEntity {

  @DiffId
  @Schema(description = "环境变量ID")
  private Long variableId;

  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户ID")
  private Long tenantId;

  @DiffField(name = "VARIABLE_CODE")
  @Schema(description = "变量编码")
  private String variableCode;

  @DiffField(name = "VARIABLE_NAME")
  @Schema(description = "变量名称")
  private String variableName;

  @DiffField(name = "DATA_TYPE")
  @Schema(description = "数据类型")
  private String dataType;

}
