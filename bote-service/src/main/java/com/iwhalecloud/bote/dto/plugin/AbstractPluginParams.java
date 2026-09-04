package com.iwhalecloud.bote.dto.plugin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

/**
 * 抽象插件
 *
 * @author qian.sisheng
 * @since 2025-04-09
 */

@Getter
@Setter
public abstract class AbstractPluginParams {
  /** 插件编码 */
  @JsonIgnore
  protected String pluginCode;

  public AbstractPluginParams(String pluginCode) {
    this.pluginCode = pluginCode;
  }
}
