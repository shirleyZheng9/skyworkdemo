package com.iwhalecloud.bote.dto.skill;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 页面模板定义
 *
 * @author chen.linfa
 * @since 2024-08-31
 */
@Getter
@Setter
@ToString
public class SimplePageTemplateDTO {
  @Schema(description = "模板类型")
  private String tamplateType;
  @Schema(description = "标题")
  private String pageTitle;
  @Schema(description = "行选择: 单选、复选")
  private Map<String, Object> rowSelection;
  @Schema(description = "表格唯一表示")
  private String rowKey;
  @Schema(description = "图标")
  private String icon;
  @Schema(description = "描述列表列数")
  private Integer column;
  @Schema(description = "布局")
  private String layout;
  @Schema(description = "文本对齐方式")
  private String labelAlign;
  @Schema(description = "页面按钮")
  private List<Map<String, Object>> controlBtns;
  @Schema(description = "页面属性")
  private List<SimplePageTemplateAttrDTO> attrs;
  @Schema(description = "子节点")
  private List<SimplePageTemplateDTO> children;
}
