package com.iwhalecloud.bote.dto.skill;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

/**
 * 工作流
 *
 * @author bianjp
 * @since 2025-03-03
 */
@Getter
@Setter
@ToString
@Hidden
public class SkillFlowWithParamDTO extends SkillFlowDTO {
  /** 变量 JSON */
  private String variableJson;
  /** 入参 JSON */
  private String requestJson;
  /** 出参 JSON */
  private String responseJson;
  /** 租户名称 */
  private String tenantName;

  public String getTenantNameOrId() {
    return StringUtils.isNotEmpty(tenantName) ? tenantName : getTenantId().toString();
  }
}
