package com.iwhalecloud.bote.dto.app.query;

import com.iwhalecloud.bote.dto.base.query.PagingQueryParams;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * 工作台应用查询参数
 *
 * @author tingyun.wang
 * @since 2025-09-08
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "工作台应用查询参数")
public class WorkbenchAppQueryParams extends PagingQueryParams {

  @Schema(description = "租户ID")
  private Long tenantId;
  @Schema(description = "工作空间ID")
  private Long spaceId;
  @Schema(description = "应用分类")
  private Long catalogItemId;
  @Schema(description = "应用名称（模糊搜索）")
  private String appName;
  @Schema(description = "应用状态（A:启用, X:停用）")
  private String appStatus;
  @Schema(description = "应用场景")
  private String appScene;
  @Schema(description = "用户ID")
  private Long userId;
  @Schema(description = "授权组织ID列表")
  private List<Long> orgIdList;

}
