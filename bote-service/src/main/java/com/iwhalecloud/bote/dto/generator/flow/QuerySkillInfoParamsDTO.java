package com.iwhalecloud.bote.dto.generator.flow;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 技能基本信息查询参数
 *
 * @author bianjp
 * @since 2025-04-16
 */
@Getter
@Setter
@ToString
@Schema(description = "技能基本信息查询参数")
public class QuerySkillInfoParamsDTO {
  @Schema(description = "租户 ID")
  private Long tenantId;
  @Schema(description = "技能 ID 列表映射, key 为技能类型, value 为技能 ID 列表")
  private Map<String, List<Long>> skillIdsMap;
}
