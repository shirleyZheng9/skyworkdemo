package com.iwhalecloud.bote.dto.base.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 敏感词查询条件
 *
 * @author bianjp
 * @since 2025-01-15
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "敏感词查询条件")
public class SensitiveWordQueryParams extends PagingQueryParams {
  @Schema(description = "是否是黑名单(T/F)")
  private String isBlack;
  @Schema(description = "关键字（模糊匹配）")
  private String keyword;
}
