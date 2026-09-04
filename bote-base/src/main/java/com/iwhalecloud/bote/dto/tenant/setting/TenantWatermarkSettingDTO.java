package com.iwhalecloud.bote.dto.tenant.setting;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 租户的水印设置
 *
 * @author chen.linfa
 * @since 2025-12-23
 */
@Getter
@Setter
@ToString
public class TenantWatermarkSettingDTO {
  /** 是否开启 */
  private Boolean enabled;
  /** 内容 */
  private String content;
  /** 基础样式 */
  private WatermarkStyle styles;
  /** 布局 */
  private WatermarkLayout layout;

  @Getter
  @Setter
  @ToString
  @NoArgsConstructor
  @AllArgsConstructor
  public static class WatermarkStyle {
    private String color;
    private Integer opacity;
    private Integer fontSize;
    private Integer angle;
  }

  @Getter
  @Setter
  @ToString
  @NoArgsConstructor
  @AllArgsConstructor
  public static class WatermarkLayout {
    private String density;
    private Integer horizontalSpacing;
    private Integer verticalSpacing;
    private String arrangement;
  }
}
