package com.iwhalecloud.bote.dto.base;

import com.iwhalecloud.bote.entity.base.EnvVariableEntity;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 环境变量
 *
 * @author qian.sisheng
 * @since 2025-11-03
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "应用环境变量")
public class EnvVariableDTO extends EnvVariableEntity {
  @Schema(description = "操作人名称")
  private String updatorName;
  @DiffField(childNode = true)
  @Schema(description = "环境变量值列表")
  private List<EnvVariableValDTO> envVariableVals;
}
