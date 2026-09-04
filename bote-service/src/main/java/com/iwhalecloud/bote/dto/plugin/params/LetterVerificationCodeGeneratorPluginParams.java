package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.dto.plugin.AbstractPluginParams;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 字母验证码生成插件参数
 *
 * @author lizuyin
 * @since 2025-11-18
 */
@Getter
@Setter
@ToString
public class LetterVerificationCodeGeneratorPluginParams extends AbstractPluginParams {
  /** 验证码长度（必填，整数类型，必须大于0） */
  private Long length;
  /** 字母类型（必填，字符串类型），可选值：upper（大写）、lower（小写）、mixed（混合） */
  private String caseType;

  public LetterVerificationCodeGeneratorPluginParams() {
    super(PluginConsts.PLUGIN_CODE_LETTER_VERIFICATION_CODE_GENERATOR);
  }
}

