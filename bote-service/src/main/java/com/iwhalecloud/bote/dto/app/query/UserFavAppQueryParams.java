package com.iwhalecloud.bote.dto.app.query;

import com.iwhalecloud.bote.dto.base.query.PagingQueryParams;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * 用户常用应用查询参数
 *
 * @author tingyun.wang
 * @since 2025-09-19
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "用户常用应用查询参数")
public class UserFavAppQueryParams extends PagingQueryParams {

  @Schema(description = "用户ID")
  private Long userId;
  @Schema(description = "租户ID")
  private Long tenantId;
  @Schema(description = "应用名称（模糊搜索）")
  private String appName;
  @Schema(description = "企业空间ID")
  private Long spaceId;
  @Schema(description = "组织ID列表")
  private List<Long> orgIds;
  @Schema(description = "是否为校验应用授权")
  private boolean checkAuth;

}
