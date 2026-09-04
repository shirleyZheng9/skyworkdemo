package com.iwhalecloud.bote.dto.skill.query;

import com.iwhalecloud.bote.dto.base.query.PagingQueryParams;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * SKILL广场管理端查询参数
 *
 * @author skill-square
 * @since 2026-03-18
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "SKILL广场管理端查询参数")
public class SkillSquareAdminQueryParams extends PagingQueryParams {

  @Schema(description = "关键词搜索（匹配 skill_name、skill_desc）")
  private String keyword;

  @Schema(description = "上架状态筛选: T-上架, F-下架，不传则不过滤")
  private String onlineStatus;

  @Schema(description = "来源筛选: square、zip 等（历史数据可能含 clawhub、skills.sh），不传则不过滤")
  private String source;

  @Schema(description = "技能类型筛选: platform、community，不传则不过滤")
  private String skillType;
}
