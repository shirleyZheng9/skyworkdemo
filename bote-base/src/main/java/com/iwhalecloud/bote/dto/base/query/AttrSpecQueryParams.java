package com.iwhalecloud.bote.dto.base.query;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 属性查询参数
 *
 * @author auto
 * @since 2024-09-13
 */
@Getter
@Setter
@ToString(callSuper = true)
public class AttrSpecQueryParams extends PagingQueryParams {
  @Schema(description = "租户 ID")
  private Long tenantId;
  @Schema(description = "模糊查询")
  private String searchContent;
  @Schema(description = "属性编码列表")
  private List<String> attrCodes;
  @Schema(description = "是否只查询平台级静态数据")
  private Boolean system;
}
