package com.iwhalecloud.bote.service.plugin.runner;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.params.Md5HashPluginParams;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;

/**
 * MD5哈希算法加密插件
 *
 * @author lizuyin
 * @since 2025-11-18
 */
@Component
public class Md5HashPlugin extends AbstractPlugin<Md5HashPluginParams> {

  public Md5HashPlugin() {
    super(Md5HashPluginParams.class);
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_MD5_HASH;
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
    children.add(ParameterSpec.newProperty("result", "MD5加密后的内容", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("message", "消息", AttrDataType.STRING));
    return ParameterSpec.newRoot(children);
  }

  @Override
  public void validateParams(Md5HashPluginParams params) {
    // src可以为null，所以这里不做非空校验
    // 如果src为null，返回result为null
  }

  @Override
  public Object doRun(Md5HashPluginParams pluginParams) {
    try {
      Map<String, Object> response = new HashMap<>();

      if (pluginParams.getSrc() == null) {
        response.put("success", true);
        response.put("message", "");
        response.put("result", null);
        return response;
      }

      String encode = DigestUtils.md5Hex(pluginParams.getSrc());
      response.put("message", "");
      response.put("success", true);
      response.put("result", encode);
      return response;
    }
    catch (Exception e) {
      logger.error("MD5哈希加密时发生异常", e);
      Map<String, Object> response = new HashMap<>();
      response.put("success", false);
      response.put("message", "MD5哈希加密失败: " + e.getMessage());
      response.put("result", null);
      return response;
    }
  }
}

