package com.iwhalecloud.bote.dto.skill;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

/**
 * 简单页面技能
 *
 * @author bianjp
 * @since 2024-12-18
 */
@Getter
@Setter
@ToString
public class SimplePageDTO {
  /** 页面 ID */
  private Long pageId;
  /** 页面名称 */
  private String pageName;
  /** 页面标题 */
  private String pageTitle;
  /** 页面编码 */
  private String pageCode;
  /** 页面来源 */
  private String pageSourceType;
  /** 页面类型 */
  private String pageType;
  /** 页面模板 */
  private Map<String, Object> pageTemplate;
  /** 入参 */
  private ParameterSpec request;

  /**
   * 转换页面对象
   */
  public static SimplePageDTO from(SkillPageDTO page) {
    SimplePageDTO dto = new SimplePageDTO();
    dto.pageId = page.getPageId();
    dto.pageName = page.getPageName();
    dto.pageTitle = page.getPageTitle();
    dto.pageCode = page.getPageCode();
    dto.pageSourceType = page.getPageSourceType();
    dto.pageType = page.getPageType();
    if (BaseConsts.PAGE_SOURCE_PLATFORM.equals(page.getPageSourceType()) && StringUtils.isNotEmpty(page.getPageTemplateJson())) {
      // 不直接反序列化为具体的对象，以避免漏掉一些属性导致前端渲染出错
      dto.pageTemplate = JsonUtil.parseJsonRequired(page.getPageTemplateJson(), new TypeReference<Map<String, Object>>() {
      });
    }
    if (StringUtils.isNotEmpty(page.getReqParamJson())) {
      dto.request = JsonUtil.parseJsonRequired(page.getReqParamJson(), ParameterSpec.class);
    }
    return dto;
  }
}
