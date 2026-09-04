package com.iwhalecloud.bote.dto.base;

import com.iwhalecloud.bote.entity.base.EnvVariableValEntity;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 环境变量值
 *
 * @author qian.sisheng
 * @since 2025-11-03
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_env_variable_val")
public class EnvVariableValDTO extends EnvVariableValEntity {

  @Schema(description = "数据类型")
  private String dataType;

}
