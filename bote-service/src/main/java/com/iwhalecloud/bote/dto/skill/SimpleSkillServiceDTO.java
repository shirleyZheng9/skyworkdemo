package com.iwhalecloud.bote.dto.skill;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 技能：简单服务
 *
 * @author bianjp
 * @since 2024-12-18
 */
@Getter
@Setter
@ToString
public class SimpleSkillServiceDTO {
  @Schema(description = "租户 ID")
  private Long tenantId;
  @Schema(description = "服务 ID")
  private Long serviceId;
  @Schema(description = "服务编码")
  private String serviceCode;
  @Schema(description = "服务名称")
  private String serviceName;
  @Schema(description = "header参数json")
  private String headerJson;
  @Schema(description = "path参数json")
  private String pathJson;
  @Schema(description = "query参数json")
  private String queryJson;
  @Schema(description = "body参数json")
  private String bodyJson;
  @Schema(description = "响应参数")
  private String responseJson;

  @JsonIgnore
  public Map<String, Object> toMap() {
    Map<String, Object> map = new HashMap<>();
    map.put("tenantId", tenantId);
    map.put("serviceId", serviceId);
    map.put("serviceCode", serviceCode);
    map.put("serviceName", serviceName);
    map.put("headerJson", headerJson);
    map.put("pathJson", pathJson);
    map.put("queryJson", queryJson);
    map.put("bodyJson", bodyJson);
    map.put("responseJson", responseJson);
    return map;
  }
}
