package com.iwhalecloud.bote.dto.skill;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author qian.sisheng
 * @since 2024/8/8
 */
@Getter
@Setter
@ToString
public class SqlOperaParams {
  @Schema(description = "数据源类型")
  private String dataSourceType;
  @Schema(description = "数据源编码")
  private Long dataSourceId;
  @Schema(description = "自定义 SQL", requiredMode = RequiredMode.REQUIRED)
  private String sql;
  @Schema(description = "SQL查询结果集类型(1: 单值, 2: 单对象, 3: 列表, 4: 分页列表)。目前后端仅对分页列表做特殊处理(入参增加)", allowableValues = {"1", "2", "3", "4" })
  private String scriptResultType;
  @Schema(description = "参数")
  private Map<String, Object> params;
  @Schema(description = "服务配置入参")
  private String reqJson;
  @Schema(description = "服务配置出参")
  private String repJson;
  @Schema(description = "sql服务ID")
  private Long serviceId;
  @Schema(description = "租户 ID")
  private Long tenantId;
}
