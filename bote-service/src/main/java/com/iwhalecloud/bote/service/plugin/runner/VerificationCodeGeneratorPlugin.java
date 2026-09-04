package com.iwhalecloud.bote.service.plugin.runner;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.params.VerificationCodeGeneratorPluginParams;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 数字验证码生成插件
 *
 * @author lizuyin
 * @since 2025-11-18
 */
@Component
public class VerificationCodeGeneratorPlugin extends AbstractPlugin<VerificationCodeGeneratorPluginParams> {

  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  public VerificationCodeGeneratorPlugin() {
    super(VerificationCodeGeneratorPluginParams.class);
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_VERIFICATION_CODE_GENERATOR;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("length", "验证码长度（必填，整数类型，必须大于0）", AttrDataType.INTEGER));
    return ParameterSpec.newRoot(children);
  }

  @Override
  public ParameterSpec createResponseParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("success", "是否成功", AttrDataType.BOOLEAN));
    children.add(ParameterSpec.newProperty("message", "消息", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("result", "生成的验证码", AttrDataType.STRING));
    return ParameterSpec.newRoot(children);
  }

  @Override
  public void validateParams(VerificationCodeGeneratorPluginParams params) {
    Assert.notNull(params.getLength(), "参数 length 不能为空");
    Assert.isTrue(params.getLength() > 0, "参数 length 必须为正整数");
  }

  @Override
  public Object doRun(VerificationCodeGeneratorPluginParams pluginParams) {
    try {
      // 生成验证码
      int length = pluginParams.getLength().intValue();
      StringBuilder codeBuilder = new StringBuilder();

      for (int i = 0; i < length; i++) {
        // 生成0-9之间的随机数字
        int digit = SECURE_RANDOM.nextInt(10);
        codeBuilder.append(digit);
      }

      String verificationCode = codeBuilder.toString();

      // 返回成功结果
      Map<String, Object> result = new HashMap<>();
      result.put("success", true);
      result.put("message", "");
      result.put("result", verificationCode);
      return result;
    }
    catch (Exception e) {
      // 静默失败，返回失败结果
      Map<String, Object> result = new HashMap<>();
      result.put("success", false);
      result.put("message", "生成验证码时发生异常: " + e.getMessage());
      result.put("result", null);
      return result;
    }
  }
}

