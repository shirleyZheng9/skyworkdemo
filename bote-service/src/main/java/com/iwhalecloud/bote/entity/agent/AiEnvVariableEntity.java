package com.iwhalecloud.bote.entity.agent;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 用户级的环境变量 Entity
 *
 * @author linmengfan
 * @since 2026-03-05
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "BT_AI_ENV_VARIABLE")
public class AiEnvVariableEntity extends BaseEntity {

  @DiffId
  @Schema(description = "主键")
  private Long id;
  @DiffField(name = "SPACE_ID")
  @Schema(description = "空间ID")
  private Long spaceId;
  @DiffField(name = "BOT_ID")
  @Schema(description = "应用ID")
  private Long botId;
  @DiffField(name = "VARIABLE_CODE")
  @Schema(description = "环境变量编码")
  private String variableCode;
  @DiffField(name = "VARIABLE_VALUE")
  @Schema(description = "环境变量值")
  private String variableValue;
}
