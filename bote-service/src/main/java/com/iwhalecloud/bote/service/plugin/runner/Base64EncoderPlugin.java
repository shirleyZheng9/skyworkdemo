package com.iwhalecloud.bote.service.plugin.runner;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.params.Base64EncoderPluginParams;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * Base64编码插件
 *
 * @author qian.sisheng
 * @since 2025-06-11
 */
@Component
public class Base64EncoderPlugin extends AbstractPlugin<Base64EncoderPluginParams> {

  public Base64EncoderPlugin() {
    super(Base64EncoderPluginParams.class);
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_BASE_64_ENCODER;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    return ParameterSpec.newRoot(Collections.singletonList(ParameterSpec.newProperty("text", "文本", AttrDataType.STRING)));
  }

  @Override
  public ParameterSpec createResponseParameter() {
    return ParameterSpec.newRoot(Collections.singletonList(ParameterSpec.newProperty("encoderText", "编码后文本", AttrDataType.STRING)));
  }

  @Override
  public void validateParams(Base64EncoderPluginParams params) {
    Assert.hasText(params.getText(), "text不能为空");
  }

  @Override
  public Object doRun(Base64EncoderPluginParams pluginParams) {
    Map<String, Object> params = new HashMap<>();
    params.put("encoderText", Base64.encodeBase64String(pluginParams.getText().getBytes(StandardCharsets.UTF_8)));
    return params;
  }

}
