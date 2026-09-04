package com.iwhalecloud.bote.dto.skill;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 技能广场导出/导入 meta.json 结构
 * <p>
 * 用于 manifest 条目及 meta.json 解析，JSON 字段为 snake_case
 *
 * @author skill-square
 * @since 2026-03-18
 */
@Getter
@Setter
@ToString
@JsonNaming(SnakeCaseStrategy.class)
@Schema(description = "技能 meta 信息（导出 zip 中 meta.json 结构）")
public class SkillMetaDTO {

  /**
   * 默认版本号
   */
  public static final String DEFAULT_VERSION = "1.0.0";
  /**
   * 默认来源
   */
  public static final String DEFAULT_SOURCE = "square";
  /**
   * 默认技能类型
   */
  public static final String DEFAULT_SKILL_TYPE = "community";
  @Schema(description = "技能编码")
  private String skillCode;
  @Schema(description = "技能名称")
  private String skillName;
  @Schema(description = "技能简介")
  private String skillDesc;
  @Schema(description = "技能类型: platform/community")
  private String skillType;
  @Schema(description = "标签，可为 JSON 字符串或数组序列化")
  private Object tags;
  @Schema(description = "版本号")
  private String version;
  @Schema(description = "来源: square/skills.sh/clawhub")
  private String source;
}
