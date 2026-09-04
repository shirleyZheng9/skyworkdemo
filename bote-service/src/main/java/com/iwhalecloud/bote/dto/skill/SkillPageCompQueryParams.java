package com.iwhalecloud.bote.dto.skill;

import com.iwhalecloud.bote.dto.base.query.PagingQueryParams;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 技能：页面组件查询参数
 *
 * @author lizuyin
 * @since 2026-01-14
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "技能：页面组件查询参数")
public class SkillPageCompQueryParams extends PagingQueryParams {
  @Schema(description = "租户ID")
  private Long tenantId;
  @Schema(description = "模糊查询（组件名称）")
  private String searchContent;
  @Schema(description = "目录ID")
  private Long catalogItemId;
}

