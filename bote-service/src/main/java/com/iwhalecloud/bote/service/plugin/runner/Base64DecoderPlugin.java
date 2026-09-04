package com.iwhalecloud.bote.service.plugin.runner;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.params.Base64DecoderPluginParams;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * Base64解码插件
 *
 * @author qian.sisheng
 * @since 2025-06-11
 */
@Component
public class Base64DecoderPlugin extends AbstractPlugin<Base64DecoderPluginParams> {

  public Base64DecoderPlugin() {
    super(Base64DecoderPluginParams.class);
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_BASE_64_DECODER;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    return ParameterSpec.newRoot(Collections.singletonList(ParameterSpec.newProperty("encoderText", "编码文本", AttrDataType.STRING)));
  }

  @Override
  public ParameterSpec createResponseParameter() {
    return ParameterSpec.newRoot(Collections.singletonList(ParameterSpec.newProperty("decoderText", "解码文本", AttrDataType.STRING)));
  }

  @Override
  public void validateParams(Base64DecoderPluginParams params) {
    Assert.notNull(params.getEncoderText(), "encoderText不能为空");
  }

  @Override
  public Object doRun(Base64DecoderPluginParams pluginParams) {
    Map<String, Object> params = new HashMap<>();
    params.put("decoderText", new String(Base64.decodeBase64(pluginParams.getEncoderText()), StandardCharsets.UTF_8));
    return params;
  }
}
