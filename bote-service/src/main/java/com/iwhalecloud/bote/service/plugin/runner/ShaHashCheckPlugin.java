package com.iwhalecloud.bote.service.plugin.runner;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.params.ShaHashCheckPluginParams;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * SHA哈希算法加密验证插件
 *
 * @author lizuyin
 * @since 2025-11-18
 */
@Component
public class ShaHashCheckPlugin extends AbstractPlugin<ShaHashCheckPluginParams> {

  /**
   * SHA1哈希值格式：40位十六进制字符串
   */
  private static final Pattern SHA1_HASH_PATTERN = Pattern.compile("^[0-9a-fA-F]{40}$");

  public ShaHashCheckPlugin() {
    super(ShaHashCheckPluginParams.class);
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_SHA_HASH_CHECK;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("input", "原始输入字符串", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("hash", "已加密的SHA密文（40位十六进制字符串）", AttrDataType.STRING));
    return ParameterSpec.newRoot(children);
  }

  @Override
  public ParameterSpec createResponseParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("success", "是否成功", AttrDataType.BOOLEAN));
    children.add(ParameterSpec.newProperty("message", "消息", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("result", "验证结果", AttrDataType.BOOLEAN));
    return ParameterSpec.newRoot(children);
  }

  @Override
  public void validateParams(ShaHashCheckPluginParams params) {
    Assert.notNull(params.getInput(), "参数 input 不能为空");
    Assert.notNull(params.getHash(), "参数 hash 不能为空");

    String hashValue = StringUtils.trimToEmpty(params.getHash());
    if (hashValue.length() != 40) {
      throw new IllegalArgumentException("参数 hash 格式错误，应为40位十六进制字符串");
    }

    if (!SHA1_HASH_PATTERN.matcher(hashValue).matches()) {
      throw new IllegalArgumentException("参数 hash 格式错误，应为40位十六进制字符串");
    }
  }

  @Override
  public Object doRun(ShaHashCheckPluginParams pluginParams) {
    try {
      // 转换hash为字符串并去除空格
      String hashValue = StringUtils.trimToEmpty(pluginParams.getHash());

      // 对input进行SHA1加密
      String inputHash = DigestUtils.sha1Hex(pluginParams.getInput());

      // 统一转换为小写进行比较（SHA哈希不区分大小写）
      String inputHashLower = StringUtils.lowerCase(inputHash);
      String hashValueLower = StringUtils.lowerCase(hashValue);

      // 比较加密结果与hash
      boolean isValid = inputHashLower.equals(hashValueLower);

      // 返回成功结果
      Map<String, Object> response = new HashMap<>();
      response.put("success", true);
      response.put("message", "");
      response.put("result", isValid);
      return response;

    }
    catch (Exception e) {
      logger.error("SHA哈希验证时发生异常", e);
      Map<String, Object> response = new HashMap<>();
      response.put("success", false);
      response.put("message", "SHA哈希验证时发生异常: " + e.getMessage());
      response.put("result", null);
      return response;
    }
  }
}

