package com.iwhalecloud.bote.service.plugin.runner;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.params.MixedVerificationCodeGeneratorPluginParams;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 混合验证码生成插件
 *
 * @author lizuyin
 * @since 2025-11-18
 */
@Component
public class MixedVerificationCodeGeneratorPlugin extends AbstractPlugin<MixedVerificationCodeGeneratorPluginParams> {

  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  private static final String DIGIT_POOL = "0123456789";

  private static final String UPPER_CASE_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

  private static final String LOWER_CASE_LETTERS = "abcdefghijklmnopqrstuvwxyz";

  private static final String MIXED_CASE_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

  public MixedVerificationCodeGeneratorPlugin() {
    super(MixedVerificationCodeGeneratorPluginParams.class);
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_MIXED_VERIFICATION_CODE_GENERATOR;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("length", "验证码长度（必填，整数类型，必须大于0）", AttrDataType.INTEGER));
    children.add(
      ParameterSpec.newProperty("caseType", "字母类型（必填，字符串类型），可选值：upper（大写）、lower（小写）、mixed（混合）",
        AttrDataType.STRING));
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
  public void validateParams(MixedVerificationCodeGeneratorPluginParams params) {
    Assert.notNull(params.getLength(), "参数 length 不能为空");
    Assert.isTrue(params.getLength() > 0, "参数 length 必须为正整数");
    Assert.notNull(params.getCaseType(), "参数 caseType 不能为空");
    String caseTypeValue = params.getCaseType().trim().toLowerCase();
    Assert.isTrue("upper".equals(caseTypeValue) || "lower".equals(caseTypeValue) || "mixed".equals(caseTypeValue),
      "参数 caseType 必须为 'upper'、'lower' 或 'mixed' 之一");
  }

  @Override
  public Object doRun(MixedVerificationCodeGeneratorPluginParams pluginParams) {
    try {
      // 获取参数
      int length = pluginParams.getLength().intValue();
      String caseTypeValue = pluginParams.getCaseType().trim().toLowerCase();

      // 根据 caseType 选择字母池
      String letterPool;
      if ("upper".equals(caseTypeValue)) {
        letterPool = UPPER_CASE_LETTERS;
      }
      else if ("lower".equals(caseTypeValue)) {
        letterPool = LOWER_CASE_LETTERS;
      }
      else {
        letterPool = MIXED_CASE_LETTERS;
      }

      // 混合池 = 数字池 + 字母池
      String mixedPool = DIGIT_POOL + letterPool;

      // 生成验证码
      StringBuilder codeBuilder = new StringBuilder();
      for (int i = 0; i < length; i++) {
        // 从混合池中随机选择一个字符
        int index = SECURE_RANDOM.nextInt(mixedPool.length());
        codeBuilder.append(mixedPool.charAt(index));
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
      result.put("message", "生成混合验证码时发生异常: " + e.getMessage());
      result.put("result", null);
      return result;
    }
  }
}

