package com.iwhalecloud.bote.dto.plugin.params;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 高德地理编码插件参数
 *
 * @author lizuyin
 * @since 2025-11-19
 */
@Getter
@Setter
@ToString
public class GaodeGeocodePluginParams {
  /** 高德API密钥 */
  private String key;
  /** 地址 */
  private String address;
}

