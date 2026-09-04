package com.iwhalecloud.bote.dto.skill.query;

import com.iwhalecloud.bote.dto.base.query.PagingQueryParams;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/**
 * 技能发布申请查询参数
 *
 * @author wangtingyun
 * @since 2026-04-03
 */
@Getter
@Setter
@ToString(callSuper = true)
public class SkillPublishApplyQueryParams extends PagingQueryParams {
  @Schema(description = "模糊查询")
  private String searchContent;
  @Schema(description = "空间 ID")
  private Long spaceId;
  @Schema(description = "审核状态")
  private Integer auditStatus;
  @Schema(description = "创建人 ID")
  private Long creatorId;
}
