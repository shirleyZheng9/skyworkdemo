package com.iwhalecloud.bote.dto.skill;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 技能：简单插件
 *
 * @author bianjp
 * @since 2024-12-18
 */
@Getter
@Setter
@ToString
public class SimpleSkillPluginDTO {
  @Schema(description = "租户 ID")
  private Long tenantId;
  @Schema(description = "插件 ID")
  private Long apiId;
  @Schema(description = "插件编码")
  private String apiCode;
  @Schema(description = "插件名称")
  private String apiName;
  @Schema(description = "插件入参")
  private String reqJson;

  @JsonIgnore
  public Map<String, Object> toMap() {
    Map<String, Object> map = new HashMap<>();
    map.put("tenantId", tenantId);
    map.put("apiId", apiId);
    map.put("apiCode", apiCode);
    map.put("apiName", apiName);
    map.put("reqJson", reqJson);
    return map;
  }
}
