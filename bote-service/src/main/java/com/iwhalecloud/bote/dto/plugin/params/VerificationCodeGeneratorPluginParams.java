package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.dto.plugin.AbstractPluginParams;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 数字验证码生成插件参数
 *
 * @author lizuyin
 * @since 2025-11-18
 */
@Getter
@Setter
@ToString
public class VerificationCodeGeneratorPluginParams extends AbstractPluginParams {
  /** 验证码长度（必填，整数类型，必须大于0） */
  private Long length;

  public VerificationCodeGeneratorPluginParams() {
    super(PluginConsts.PLUGIN_CODE_VERIFICATION_CODE_GENERATOR);
  }
}

