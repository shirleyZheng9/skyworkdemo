package com.iwhalecloud.bote.dto.skill;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bote.entity.skill.ServicePlatformEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 技能：API 平台 DTO
 *
 * @author auto
 * @since 2024-09-17
 */
@Getter
@Setter
@ToString(callSuper = true)
public class ServicePlatformDTO extends ServicePlatformEntity {
  @DiffField(childNode = true)
  @Schema(description = "网关列表")
  private List<ServiceGatewayDTO> gateways;
  @Schema(description = "创建人名称")
  private String creatorName;
  @Schema(description = "修改人名称")
  private String updatorName;
}
