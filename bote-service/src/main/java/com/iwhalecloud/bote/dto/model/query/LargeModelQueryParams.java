package com.iwhalecloud.bote.dto.model.query;

import com.iwhalecloud.bote.dto.base.query.PagingQueryParams;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 大模型查询参数
 *
 * @author auto
 * @since 2024-09-20
 */
@Getter
@Setter
@ToString(callSuper = true)
public class LargeModelQueryParams extends PagingQueryParams {
  @Schema(description = "模型名称或编码")
  private String modelNameOrCode;
  @Schema(description = "模型类型")
  private String modelType;
  @Schema(description = "租户ID")
  private Long tenantId;
  @Schema(description = "产品类型")
  private String productType;
  @Schema(description = "产品来源")
  private String sourceFrom;
  @Schema(description = "企业空间ID，为空则非企业大模型，非空则为对应企业空间的大模型")
  private Long spaceId;
  @Schema(description = "用户ID")
  private Long userId;
  @Schema(description = "是否启用")
  private String isEnabled;
}
