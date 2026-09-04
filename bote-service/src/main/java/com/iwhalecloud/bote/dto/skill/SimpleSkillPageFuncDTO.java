package com.iwhalecloud.bote.dto.skill;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 技能：简单页面函数
 *
 * @author bianjp
 * @since 2024-12-18
 */
@Getter
@Setter
@ToString
public class SimpleSkillPageFuncDTO {
  @Schema(description = "租户 ID")
  private Long tenantId;
  @Schema(description = "页面函数 ID")
  private Long pageFuncId;
  @Schema(description = "页面函数编码")
  private String funcCode;
  @Schema(description = "页面函数名称")
  private String funcName;
  @Schema(description = "页面函数入参")
  private String reqJson;

  @JsonIgnore
  public Map<String, Object> toMap() {
    Map<String, Object> map = new HashMap<>();
    map.put("tenantId", tenantId);
    map.put("pageFuncId", pageFuncId);
    map.put("funcCode", funcCode);
    map.put("funcName", funcName);
    map.put("reqJson", reqJson);
    return map;
  }
}
