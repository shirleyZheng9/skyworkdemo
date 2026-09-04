package com.iwhalecloud.bote.dto.base;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 操作指引
 *
 * @author chen.linfa
 * @since 2025-07-15
 */
@Getter
@Setter
@ToString
public class GuidanceDTO {
  @Schema(description = "步骤编码")
  private String key;
  @Schema(description = "步骤名称")
  private String label;
  @Schema(description = "步骤图标")
  private String icon;
  @Schema(description = "内容")
  private List<GuidanceImageDTO> imgs;
  @Schema(description = "子标题")
  private List<GuidanceDetailDTO> details;

  @Getter
  @Setter
  @ToString
  @NoArgsConstructor
  @AllArgsConstructor
  public static class GuidanceImageDTO {
    @Schema(description = "内容标题")
    private String title;
    @Schema(description = "内容图片")
    private String base64;
  }

  @Getter
  @Setter
  @ToString
  @NoArgsConstructor
  @AllArgsConstructor
  public static class GuidanceDetailDTO {
    @Schema(description = "子标题")
    private String title;
    @Schema(description = "子内容")
    private String description;
  }
}
