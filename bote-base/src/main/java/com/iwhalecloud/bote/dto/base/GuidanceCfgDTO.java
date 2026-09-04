package com.iwhalecloud.bote.dto.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author chen.linfa
 * @since 2025-07-15
 */
@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GuidanceCfgDTO {

  @Schema(description = "主键ID")
  private Long id;
  @Schema(description = "步骤名称")
  private String name;
  @Schema(description = "步骤编码")
  private String code;
  @Schema(description = "步骤图标")
  private String titleIcon;
  @Schema(description = "内容图片")
  private String textIcon;
  @Schema(description = "子标题")
  private String subTitle;
  @Schema(description = "子内容")
  private String subText;
  @Schema(description = "排序")
  private Integer sortby;
  @Schema(description = "引导类型")
  private String guideType;
  @Schema(description = "分组ID")
  private Long groupId;

  @Schema(description = "详情子列表")
  private List<GuidanceCfgDTO> details;

}
