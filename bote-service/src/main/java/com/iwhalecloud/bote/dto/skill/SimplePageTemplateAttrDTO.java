package com.iwhalecloud.bote.dto.skill;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 页面模板属性定义
 *
 * @author chen.linfa
 * @since 2024-08-31
 */
@Getter
@Setter
@ToString
public class SimplePageTemplateAttrDTO {
  @Schema(description = "属性名称")
  private String attrName;
  @Schema(description = "属性编码")
  private String attrCode;
  @Schema(description = "标题")
  private String title;
  @Schema(description = "页面组件类型", example = "input select")
  private String componentType;
  @Schema(description = "默认值，支持动态变量，例如：${root.cust_id}")
  private String defaultValue;
  @Schema(description = "是否必填")
  private Boolean required;
  @Schema(description = "是否隐藏")
  private Boolean display;
  @Schema(description = "下拉框的静态编码")
  private String staticCode;
  @Schema(description = "下拉框的动态取值，例如：{\"label\":\"${root.scenes.sceneName}\",\"value\":\"${root.scenes.sceneId}\"}")
  private Map<String, Object> options;
  @Schema(description = "校验规则")
  private List<Map<String, Object>> rules;
  @Schema(description = "表格值列表")
  private Map<String, Object> tableValues;
  @Schema(description = "表单静态数据")
  private Map<String, Object> data;
}
