package com.iwhalecloud.bote.dto.skill;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 技能：简单 SQL
 *
 * @author bianjp
 * @since 2024-12-18
 */
@Getter
@Setter
@ToString
public class SimpleSkillSqlDTO {
  @Schema(description = "租户 ID")
  private Long tenantId;
  @Schema(description = "服务 ID")
  private Long serviceId;
  @Schema(description = "服务编码")
  private String serviceCode;
  @Schema(description = "服务名称")
  private String serviceName;
  @Schema(description = "服务入参")
  private String reqJson;
  @Schema(description = "服务出参")
  private String respJson;

  @JsonIgnore
  public Map<String, Object> toMap() {
    Map<String, Object> map = new HashMap<>();
    map.put("tenantId", tenantId);
    map.put("serviceId", serviceId);
    map.put("serviceCode", serviceCode);
    map.put("serviceName", serviceName);
    map.put("reqJson", reqJson);
    map.put("respJson", respJson);
    return map;
  }
}
