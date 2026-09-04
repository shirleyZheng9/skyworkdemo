package com.iwhalecloud.bote.dto.skill;

import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

/**
 * 简单服务函数技能
 *
 * @author bianjp
 * @since 2024-12-18
 */
@Getter
@Setter
@ToString
public class SimpleFunctionSkillDTO {
  /** 函数 ID */
  private Long funcId;
  /** 函数名称 */
  private String funcName;
  /** 函数编码 */
  private String funcCode;
  /** 函数类型 */
  private String funcType;
  /** 脚本内容 */
  private String scriptContent;
  /** 入参 */
  private ParameterSpec request;
  /** 出参 */
  private ParameterSpec response;
  /** Python 包 */
  private String pyPackage;

  /**
   * 转换服务函数对象
   */
  public static SimpleFunctionSkillDTO from(SkillFunctionDTO function) {
    SimpleFunctionSkillDTO dto = new SimpleFunctionSkillDTO();
    dto.funcId = function.getFuncId();
    dto.funcName = function.getFuncName();
    dto.funcCode = function.getFuncCode();
    dto.funcType = function.getFuncType();
    dto.scriptContent = function.getScriptJson();
    dto.pyPackage = function.getPyPackage();
    if (StringUtils.isNotEmpty(function.getReqJson())) {
      dto.request = JsonUtil.parseJsonRequired(function.getReqJson(), ParameterSpec.class);
    }
    if (StringUtils.isNotEmpty(function.getRespJson())) {
      dto.response = JsonUtil.parseJsonRequired(function.getRespJson(), ParameterSpec.class);
    }
    return dto;
  }
}
