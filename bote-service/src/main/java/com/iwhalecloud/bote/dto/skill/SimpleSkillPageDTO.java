package com.iwhalecloud.bote.dto.skill;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 技能：简单页面
 *
 * @author bianjp
 * @since 2024-12-18
 */
@Getter
@Setter
@ToString
public class SimpleSkillPageDTO {
  @Schema(description = "租户 ID")
  private Long tenantId;
  @Schema(description = "页面 ID")
  private Long pageId;
  @Schema(description = "页面编码")
  private String pageCode;
  @Schema(description = "页面名称")
  private String pageName;
  @Schema(description = "页面入参")
  private String reqParamJson;
  @Schema(description = "页面来源类型")
  private String pageSourceType;

  @JsonIgnore
  public Map<String, Object> toMap() {
    Map<String, Object> map = new HashMap<>();
    map.put("tenantId", tenantId);
    map.put("pageId", pageId);
    map.put("pageCode", pageCode);
    map.put("pageName", pageName);
    map.put("reqParamJson", reqParamJson);
    map.put("pageSourceType", pageSourceType);
    return map;
  }
}
