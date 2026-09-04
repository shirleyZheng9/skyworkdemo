package com.iwhalecloud.bote.dto.skill;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 数据源向量化参数
 *
 * @author qian.sisheng
 * @since 2024/11/14
 */
@Getter
@Setter
@ToString
public class DataSourceVectorParams {
  @Schema(description = "数据源实例ID")
  private Long dataSourceInstId;
  @Schema(description = "租户ID")
  private Long tenantId;
}
