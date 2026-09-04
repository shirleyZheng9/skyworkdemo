package com.iwhalecloud.bote.dto.skill;

import com.iwhalecloud.bote.entity.skill.SkillServiceEntity;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 技能：API DTO
 *
 * @author auto
 * @since 2024-09-15
 */
@Getter
@Setter
@ToString(callSuper = true)
public class SkillServiceDTO extends SkillServiceEntity {
  @Schema(description = "修改人名称")
  private String updatorName;
  @Schema(description = "创建人名称")
  private String creatorName;
  @Schema(description = "服务平台")
  private ServicePlatformDTO platform;
  @Schema(description = "模拟响应报文")
  @DiffField(childNode = true)
  private List<ServiceMockDTO> serviceMockList;
  @Schema(description = "复制的API服务ID")
  private Long copyServiceId;
  @Schema(description = "灵犀应用ID")
  private Long appId;
  @Schema(description = "灵犀编排服务ID列表")
  private List<Long> serviceIds;
}
