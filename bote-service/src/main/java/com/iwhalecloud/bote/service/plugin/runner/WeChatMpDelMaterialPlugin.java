package com.iwhalecloud.bote.service.plugin.runner;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.util.WeChatMpApiUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.params.WeChatMpDelMaterialParams;
import java.util.Arrays;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 微信公众号删除素材
 *
 * @author fan.cong
 * @since 2025-08-18
 */
@Component
public class WeChatMpDelMaterialPlugin extends AbstractPlugin<WeChatMpDelMaterialParams> {

  public WeChatMpDelMaterialPlugin() {
    super(WeChatMpDelMaterialParams.class);
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_WE_CHAT_MP_DEL_MATERIAL;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    return ParameterSpec.newRoot(Arrays.asList(ParameterSpec.newProperty("mediaId", "素材ID", AttrDataType.STRING),
      ParameterSpec.newProperty("accessToken", "公众号access_token", AttrDataType.STRING)));
  }

  @Override
  public ParameterSpec createResponseParameter() {
    return ParameterSpec.newRoot(Arrays.asList(ParameterSpec.newProperty("message", "执行信息", AttrDataType.STRING),
      ParameterSpec.newProperty("mediaId", "公众号素材库中素材Id", AttrDataType.STRING)));
  }

  @Override
  public void validateParams(WeChatMpDelMaterialParams params) {
    Assert.notNull(params.getMediaId(), "素材ID不能为空");
    Assert.notNull(params.getAccessToken(), "公众号access_token不能为空");
  }

  @Override
  public Object doRun(WeChatMpDelMaterialParams pluginParams) {
    return WeChatMpApiUtil.delMaterial(pluginParams.getAccessToken(), pluginParams.getMediaId());
  }
}