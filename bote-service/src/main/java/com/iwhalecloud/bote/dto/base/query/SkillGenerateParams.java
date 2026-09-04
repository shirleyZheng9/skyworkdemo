package com.iwhalecloud.bote.dto.base.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * AI 生成场景 skill 参数
 *
 * @author chen.linfa
 * @since 2026-04-30
 */
@Getter
@Setter
@ToString
public class SkillGenerateParams {
  @Schema(description = "租户 ID")
  private Long tenantId;
  @Schema(description = "应用 ID")
  private Long appId;
  @Schema(description = "场景 ID")
  private Long sceneId;
  @Schema(description = "场景名称")
  private String sceneName;
  @Schema(description = "场景执行步骤")
  private String stepContent;

  @Schema(description = "skill 名称")
  private String skillName;
}
