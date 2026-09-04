package com.iwhalecloud.bote.service.plugin.runner;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.util.AesUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.params.AesEncryptPluginParams;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * AES 加密插件
 *
 * @author qian.sisheng
 * @since 2025-06-11
 */
@Component
public class AesEncryptPlugin extends AbstractPlugin<AesEncryptPluginParams> {
  public AesEncryptPlugin() {
    super(AesEncryptPluginParams.class);
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_AES_ENCRYPT;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("text", "待加密内容", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("aesKey", "AES密钥", AttrDataType.STRING));
    return ParameterSpec.newRoot(children);
  }

  @Override
  public ParameterSpec createResponseParameter() {
    return ParameterSpec.newRoot(Collections.singletonList(ParameterSpec.newProperty("encryptText", "加密后的内容", AttrDataType.STRING)));
  }

  @Override
  public void validateParams(AesEncryptPluginParams params) {
    Assert.notNull(params.getText(), "text不能为空");
    Assert.notNull(params.getAesKey(), "aesKey不能为空");
  }

  @Override
  public Object doRun(AesEncryptPluginParams pluginParams) {
    Map<String, Object> params = new HashMap<>();
    params.put("encryptText", AesUtil.aesEncrypt(pluginParams.getText(), pluginParams.getAesKey()));
    return params;
  }
}
