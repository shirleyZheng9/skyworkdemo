package com.iwhalecloud.bote.dto.skill.query;

import com.iwhalecloud.bote.dto.base.query.PagingQueryParams;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 技能安装记录查询参数
 *
 * @author skill-square
 * @since 2026-03-19
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "技能安装记录查询参数")
public class SkillInstallLogQueryParams extends PagingQueryParams {

  @Schema(description = "技能ID，不传则不过滤")
  private Long skillId;

  @Schema(description = "关键词搜索（匹配技能名称、智能体名称）")
  private String keyword;

  @Schema(description = "租户ID，不传则不过滤")
  private Long tenantId;

  @Schema(description = "安装来源: square-广场, dialogue-对话，不传则不过滤")
  private String installSource;

  @Schema(description = "安装时间起始，格式：yyyy-MM-dd HH:mm:ss，如 2026-01-01 00:00:00")
  private String startTime;

  @Schema(description = "安装时间截止，格式：yyyy-MM-dd HH:mm:ss，如 2026-12-31 23:59:59")
  private String endTime;
}
