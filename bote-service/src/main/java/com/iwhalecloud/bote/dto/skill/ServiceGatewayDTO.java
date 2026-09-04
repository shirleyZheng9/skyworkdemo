package com.iwhalecloud.bote.dto.skill;

import com.iwhalecloud.bote.entity.skill.ServiceGatewayEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 技能：API 网关 DTO
 *
 * @author auto
 * @since 2024-09-17
 */
@Getter
@Setter
@ToString(callSuper = true)
public class ServiceGatewayDTO extends ServiceGatewayEntity {
  @Schema(description = "平台名称")
  private String platformName;
  @Schema(description = "平台编码")
  private String platformCode;
}
