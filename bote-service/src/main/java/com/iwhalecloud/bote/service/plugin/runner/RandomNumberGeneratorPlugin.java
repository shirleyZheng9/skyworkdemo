package com.iwhalecloud.bote.service.plugin.runner;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.params.RandomNumberGeneratorPluginParams;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 随机数生成插件
 *
 * @author lizuyin
 * @since 2025-11-19
 */
@Component
public class RandomNumberGeneratorPlugin extends AbstractPlugin<RandomNumberGeneratorPluginParams> {

  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  private static final double DEFAULT_MIN = 0.0;

  private static final double DEFAULT_MAX = 100.0;

  public RandomNumberGeneratorPlugin() {
    super(RandomNumberGeneratorPluginParams.class);
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_RANDOM_NUMBER_GENERATOR;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("min", "最小值（可选，数字类型）", AttrDataType.NUMBER));
    children.add(ParameterSpec.newProperty("max", "最大值（可选，数字类型）", AttrDataType.NUMBER));
    children.add(
      ParameterSpec.newProperty("precision", "小数位数（可选，整数类型，必须为非负整数）", AttrDataType.INTEGER));
    return ParameterSpec.newRoot(children);
  }

  @Override
  public ParameterSpec createResponseParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("success", "是否成功", AttrDataType.BOOLEAN));
    children.add(ParameterSpec.newProperty("message", "消息，成功时为空字符串，失败时包含错误信息", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("result", "生成的随机数，失败时为null", AttrDataType.NUMBER));
    return ParameterSpec.newRoot(children);
  }

  @Override
  public void validateParams(RandomNumberGeneratorPluginParams params) {
    // 校验 precision 参数
    if (params.getPrecision() != null) {
      Assert.isTrue(params.getPrecision() >= 0, "参数 precision 必须为非负整数");
    }

    // 如果 min 和 max 都不为空，校验 min 不能大于 max
    if (params.getMin() != null && params.getMax() != null) {
      Assert.isTrue(params.getMin() <= params.getMax(), "参数 min 不能大于 max");
    }
  }

  @Override
  public Object doRun(RandomNumberGeneratorPluginParams pluginParams) {
    Map<String, Object> result = new HashMap<>();
    try {
      // 设置默认范围
      double minValue = pluginParams.getMin() != null ? pluginParams.getMin() : DEFAULT_MIN;
      double maxValue = pluginParams.getMax() != null ? pluginParams.getMax() : DEFAULT_MAX;

      // 生成随机数
      double randomValue = SECURE_RANDOM.nextDouble() * (maxValue - minValue) + minValue;

      // 根据精度进行四舍五入
      long precisionValue = pluginParams.getPrecision() != null ? pluginParams.getPrecision() : 0;
      if (precisionValue > 0) {
        BigDecimal bd = BigDecimal.valueOf(randomValue);
        bd = bd.setScale((int) precisionValue, RoundingMode.HALF_UP);
        randomValue = bd.doubleValue();
      }
      else {
        randomValue = Math.round(randomValue);
      }

      result.put("success", true);
      result.put("message", "");
      result.put("result", randomValue);
    }
    catch (Exception e) {
      logger.error("生成随机数时发生异常", e);
      result.put("success", false);
      result.put("message", "生成随机数时发生异常: " + e.getMessage());
      result.put("result", null);
    }
    return result;
  }
}

