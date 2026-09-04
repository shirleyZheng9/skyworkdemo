package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.dto.plugin.AbstractPluginParams;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 随机数生成插件参数
 *
 * @author lizuyin
 * @since 2025-11-19
 */
@Getter
@Setter
@ToString
public class RandomNumberGeneratorPluginParams extends AbstractPluginParams {
  /** 最小值（可选，数字类型） */
  private Double min;
  /** 最大值（可选，数字类型） */
  private Double max;
  /** 小数位数（可选，整数类型，必须为非负整数） */
  private Long precision;

  public RandomNumberGeneratorPluginParams() {
    super(PluginConsts.PLUGIN_CODE_RANDOM_NUMBER_GENERATOR);
  }
}

