package com.iwhalecloud.bote.service.plugin.runner;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.params.ShaHashPluginParams;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * SHA哈希算法加密插件
 *
 * @author lizuyin
 * @since 2025-11-17
 */
@Component
public class ShaHashPlugin extends AbstractPlugin<ShaHashPluginParams> {

  public ShaHashPlugin() {
    super(ShaHashPluginParams.class);
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_SHA_HASH;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("src", "待加密内容", AttrDataType.STRING));
    return ParameterSpec.newRoot(children);
  }

  @Override
  public ParameterSpec createResponseParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("success", "是否成功", AttrDataType.BOOLEAN));
    children.add(ParameterSpec.newProperty("message", "消息", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("result", "SHA1加密后的内容", AttrDataType.STRING));
    return ParameterSpec.newRoot(children);
  }

  @Override
  public void validateParams(ShaHashPluginParams params) {
    Assert.notNull(params.getSrc(), "src不能为空");
  }

  @Override
  public Object doRun(ShaHashPluginParams pluginParams) {
    try {
      String encode = DigestUtils.sha1Hex(pluginParams.getSrc());
      Map<String, Object> response = new HashMap<>();
      response.put("success", true);
      response.put("message", "");
      response.put("result", encode);
      return response;
    }
    catch (Exception e) {
      logger.error("SHA哈希加密时发生异常", e);
      Map<String, Object> response = new HashMap<>();
      response.put("success", false);
      response.put("message", "SHA哈希加密失败: " + e.getMessage());
      response.put("result", null);
      return response;
    }
  }
}

