package com.iwhalecloud.bote.dto.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 系统参数配置 DTO
 *
 * @author auto
 * @since 2024-09-14
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
public class DcCfgDTO {
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
  @Schema(description = "配置类型(1: 开关; 2: 参数配置; 3:模型配置;")
  private String type;
  @Schema(description = "子类型, model 大模型;select:下拉框;image:图片base64")
  private String subType;
  @Schema(description = "是否需要修改(T/F)")
  private String needUpdate;
  @Schema(description = "取值来源(特殊标记或 JSON 字符串)")
  private String valueSource;
  @Schema(description = "备注")
  private String remark;
  @Schema(description = "表编码")
  private String tableCode;
  @Schema(description = "参数组名称")
  private String paramGroupName;
  @Schema(description = "关联参数编码")
  private String relParamCodes;
  @Schema(description = "组里面的参数列表")
  private List<DcCfgSimpleDTO> children;
}
