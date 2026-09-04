package com.iwhalecloud.bote.service.plugin.runner;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.params.Md5HashCheckPluginParams;
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
 * MD5哈希算法加密验证插件
 *
 * @author lizuyin
 * @since 2025-11-18
 */
@Component
public class Md5HashCheckPlugin extends AbstractPlugin<Md5HashCheckPluginParams> {

  /**
   * MD5哈希值格式：32位十六进制字符串
   */
  private static final Pattern MD5_HASH_PATTERN = Pattern.compile("^[0-9a-fA-F]{32}$");

  public Md5HashCheckPlugin() {
    super(Md5HashCheckPluginParams.class);
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_MD5_HASH_CHECK;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("hash", "已加密的MD5密文（32位十六进制字符串）", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("input", "原始输入字符串", AttrDataType.STRING));
    return ParameterSpec.newRoot(children);
  }

  @Override
  public ParameterSpec createResponseParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("success", "是否成功", AttrDataType.BOOLEAN));
    children.add(ParameterSpec.newProperty("result", "验证结果", AttrDataType.BOOLEAN));
    children.add(ParameterSpec.newProperty("message", "消息", AttrDataType.STRING));
    return ParameterSpec.newRoot(children);
  }

  @Override
  public void validateParams(Md5HashCheckPluginParams params) {
    Assert.notNull(params.getHash(), "参数 hash 不能为空");
    Assert.notNull(params.getInput(), "参数 input 不能为空");

    String hashValue = StringUtils.trimToEmpty(params.getHash());
    if (hashValue.length() != 32) {
      throw new IllegalArgumentException("参数 hash 格式错误，应为32位十六进制字符串");
    }

    if (!MD5_HASH_PATTERN.matcher(hashValue).matches()) {
      throw new IllegalArgumentException("参数 hash 格式错误，应为32位十六进制字符串");
    }
  }

  @Override
  public Object doRun(Md5HashCheckPluginParams pluginParams) {
    try {
      // 转换hash为字符串并去除空格
      String hashValue = StringUtils.trimToEmpty(pluginParams.getHash());

      // 对input进行MD5加密
      String inputHash = DigestUtils.md5Hex(pluginParams.getInput());

      // 统一转换为小写进行比较（MD5哈希不区分大小写）
      String hashValueLower = StringUtils.lowerCase(hashValue);
      String inputHashLower = StringUtils.lowerCase(inputHash);

      // 比较加密结果与hash
      boolean isValid = inputHashLower.equals(hashValueLower);

      // 返回成功结果
      Map<String, Object> response = new HashMap<>();
      response.put("success", true);
      response.put("result", isValid);
      response.put("message", "");
      return response;

    }
    catch (Exception e) {
      logger.error("MD5哈希验证时发生异常", e);
      Map<String, Object> response = new HashMap<>();
      response.put("success", false);
      response.put("message", "MD5哈希验证时发生异常: " + e.getMessage());
      response.put("result", null);
      return response;
    }
  }
}

