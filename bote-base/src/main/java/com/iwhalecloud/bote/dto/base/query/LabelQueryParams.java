package com.iwhalecloud.bote.dto.base.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 标签查询参数
 *
 * @author auto
 * @since 2024-09-13
 */
@Getter
@Setter
@ToString
public class LabelQueryParams {
  @Schema(description = "租户 ID")
  private Long tenantId;
  @Schema(description = "标签类型")
  private String labelType;
  @Schema(description = "标签名称")
  private String labelName;
}
