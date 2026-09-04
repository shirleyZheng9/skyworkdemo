package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.dto.plugin.AbstractPluginParams;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文生图提示词优化插件参数
 *
 * @author lizuyin
 * @since 2025-11-19
 */
@Getter
@Setter
@ToString
public class PromptOptimizationPluginParams extends AbstractPluginParams {
  /** 提示词内容（必填，字符串类型），用户输入的提示词 */
  private String prompt;

  public PromptOptimizationPluginParams() {
    super(PluginConsts.PLUGIN_CODE_PROMPT_OPTIMIZATION);
  }
}

