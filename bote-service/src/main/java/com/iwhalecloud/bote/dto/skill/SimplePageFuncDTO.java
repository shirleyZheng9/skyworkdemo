package com.iwhalecloud.bote.dto.skill;

import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

/**
 * 简单页面函数技能
 *
 * @author bianjp
 * @since 2024-12-18
 */
@Getter
@Setter
@ToString
public class SimplePageFuncDTO {
  /** 页面函数 ID */
  private Long pageFuncId;
  /** 租户 ID */
  private Long tenantId;
  /** 函数编码 */
  private String funcCode;
  /** 函数名称 */
  private String funcName;
  /** 入参 */
  private ParameterSpec request;

  /**
   * 转换页面函数对象
   */
  public static SimplePageFuncDTO from(SkillPageFuncDTO pageFunc) {
    SimplePageFuncDTO dto = new SimplePageFuncDTO();
    dto.setTenantId(pageFunc.getTenantId());
    dto.pageFuncId = pageFunc.getPageFuncId();
    dto.funcCode = pageFunc.getFuncCode();
    dto.funcName = pageFunc.getFuncName();
    if (StringUtils.isNotEmpty(pageFunc.getReqJson())) {
      dto.request = JsonUtil.parseJsonRequired(pageFunc.getReqJson(), ParameterSpec.class);
    }
    return dto;
  }
}
