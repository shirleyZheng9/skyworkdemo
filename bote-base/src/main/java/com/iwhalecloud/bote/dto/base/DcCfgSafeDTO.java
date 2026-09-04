package com.iwhalecloud.bote.dto.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

/**
 * 系统安全策略相关参数配置 DTO
 *
 * @author wang.tingyun
 * @since 2025-08-19
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
public class DcCfgSafeDTO {
  @Schema(description = "主键")
  private Long configId;
  @Schema(description = "参数编码")
  private String paramCode;
  @Schema(description = "参数值")
  private String paramVal;
  @Schema(description = "参数名称")
  private String paramName;
  @Schema(description = "参数描述")
  private String paramDesc;
  @Schema(description = "参数组名称")
  private String paramGroupName;
  @Schema(description = "排序")
  private Integer sort;
  @Schema(description = "关联参数编码")
  private String relParamCodes;
  @Schema(description = "配置项组件类型(switch:开关,select:下拉框,input:输入框)")
  private String cfgCompType;
  @Schema(description = "配置项组件属性")
  private String cfgCompProps;
  @Schema(description = "配置项组件属性对象")
  private Map<String, Object> cfgCompPropsObj;
  @Schema(description = "配置项显隐表达式")
  private String cfgVisibleExpr;
  @Schema(description = "配置项图标")
  private String cfgIcon;
  @Schema(description = "配置子列表")
  private List<DcCfgSafeDTO> children;
}
