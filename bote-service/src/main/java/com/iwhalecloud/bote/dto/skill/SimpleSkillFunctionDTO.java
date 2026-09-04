package com.iwhalecloud.bote.dto.skill;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 技能：简单函数
 *
 * @author bianjp
 * @since 2024-12-18
 */
@Getter
@Setter
@ToString
public class SimpleSkillFunctionDTO {
  @Schema(description = "租户 ID")
  private Long tenantId;
  @Schema(description = "函数 ID")
  private Long funcId;
  @Schema(description = "函数编码")
  private String funcCode;
  @Schema(description = "函数名称")
  private String funcName;
  @Schema(description = "函数入参")
  private String reqJson;
  @Schema(description = "函数出参")
  private String respJson;
  @Schema(description = "脚本内容")
  private String scriptJson;

  @JsonIgnore
  public Map<String, Object> toMap() {
    Map<String, Object> map = new HashMap<>();
    map.put("tenantId", tenantId);
    map.put("funcId", funcId);
    map.put("funcCode", funcCode);
    map.put("funcName", funcName);
    map.put("reqJson", reqJson);
    map.put("respJson", respJson);
    map.put("scriptJson", scriptJson);
    return map;
  }
}
