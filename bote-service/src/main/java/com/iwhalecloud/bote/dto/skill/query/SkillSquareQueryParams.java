package com.iwhalecloud.bote.dto.skill.query;

import com.iwhalecloud.bote.dto.base.query.PagingQueryParams;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * SKILL广场查询参数
 *
 * @author skill-square
 * @since 2026-03-18
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "SKILL广场查询参数")
public class SkillSquareQueryParams extends PagingQueryParams {
  @Schema(description = "类型筛选: platform-平台技能, community-社区技能, hot-热门(最多50条，按安装数排序), all-全部")
  private String type;
  @Schema(description = "关键词搜索（匹配 skill_name、tags、skill_desc）")
  private String keyword;
  @Schema(description = "空间ID")
  private Long spaceId;
  @Schema(description = "租户ID")
  private Long tenantId;
  @Schema(description = "是否已安装")
  private Boolean installed;
}
