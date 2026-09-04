package com.iwhalecloud.bote.dto.app.query;

import com.iwhalecloud.bote.dto.base.query.PagingQueryParams;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 网页应用查询参数
 *
 * @author tingyun.wang
 * @since 2025-09-05
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "网页应用查询参数")
public class WebAppQueryParams extends PagingQueryParams {

  @Schema(description = "租户ID")
  private Long tenantId;
  @Schema(description = "应用名称（模糊搜索）")
  private String appName;
  @Schema(description = "工作空间ID")
  private Long spaceId;

}
