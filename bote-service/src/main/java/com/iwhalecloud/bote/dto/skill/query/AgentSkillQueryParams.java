package com.iwhalecloud.bote.dto.skill.query;

import com.iwhalecloud.bote.dto.base.query.PagingQueryParams;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Agent Skill 查询参数
 *
 * @author bianjp
 * @since 2026-02-03
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "Agent Skill 查询参数")
public class AgentSkillQueryParams extends PagingQueryParams {
  @Schema(description = "租户 ID")
  private Long tenantId;
  @Schema(description = "目录 ID")
  private Long catalogItemId;
  @Schema(description = "关键字(模糊搜索名称、编码)")
  private String keyword;
  @Schema(description = "空间ID")
  private Long spaceId;
  @Schema(description = "用户ID")
  private Long userId;
  @Schema(description = "是否是用户创建的")
  private Boolean isCreated;
  @Schema(description = "技能类型")
  private String skillType;
  @Schema(description = "来源：平台:platform 租户:tenant")
  private String sourceFrom;
}
