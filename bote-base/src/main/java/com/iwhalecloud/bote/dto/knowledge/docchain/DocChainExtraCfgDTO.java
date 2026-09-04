package com.iwhalecloud.bote.dto.knowledge.docchain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * DocChain 主题扩展参数定义
 */
@Getter
@Setter
@ToString
public class DocChainExtraCfgDTO {
  @Schema(description = "主键")
  private Long id;
  @Schema(description = "名称")
  private String name;
  @Schema(description = "编码")
  private String code;
  @Schema(description = "提示信息")
  private String tip;
  @Schema(description = "表单控件类型", example = "Input/InputNumber/TextArea/Select")
  private String compType;
  @Schema(description = "Select类型对应的静态词典")
  private String staticCode;
  @Schema(description = "Select类型对应的可选内容")
  private String setterOptions;
  @Schema(description = "默认值")
  private String defaultValue;
  @Schema(description = "分组名称")
  private String groupName;
  @Schema(description = "分组编码")
  private String groupCode;
  @Schema(description = "是否隐藏")
  private String isHidden;
  @Schema(description = "是否必填")
  private String isRequired;
  @Schema(description = "是否重新构建")
  private String isRedo;
  @Schema(description = "是否根节点")
  private String isRoot;
  @Schema(description = "排序")
  private Integer sortby;
  @Schema(description = "是否动态参数")
  private String isDynamic;
  @Schema(description = "条件json")
  private String relationJson;
  @Schema(description = "前端交付保留原来的分组能力，添加一个字段进行分组")
  private String extGroupName;
}
