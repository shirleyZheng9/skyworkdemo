package com.iwhalecloud.bote.service.plugin.runner;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.util.WeChatMpApiUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.params.WeChatMpDelDraftParams;
import java.util.Arrays;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 微信公众号删除草稿
 *
 * @author fan.cong
 * @since 2025-08-18
 */
@Component
public class WeChatMpDelDraftPlugin extends AbstractPlugin<WeChatMpDelDraftParams> {

  public WeChatMpDelDraftPlugin() {
    super(WeChatMpDelDraftParams.class);
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_WE_CHAT_MP_DEL_DRAFT;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    return ParameterSpec.newRoot(Arrays.asList(ParameterSpec.newProperty("mediaId", "素材ID", AttrDataType.STRING),
      ParameterSpec.newProperty("accessToken", "公众号access_token", AttrDataType.STRING)));
  }

  @Override
  public ParameterSpec createResponseParameter() {
    return ParameterSpec.newRoot(Arrays.asList(ParameterSpec.newProperty("message", "执行信息", AttrDataType.STRING),
      ParameterSpec.newProperty("mediaId", "草稿Id", AttrDataType.STRING)));
  }

  @Override
  public void validateParams(WeChatMpDelDraftParams params) {
    Assert.notNull(params.getMediaId(), "草稿ID不能为空");
    Assert.notNull(params.getAccessToken(), "公众号access_token不能为空");
  }

  @Override
  public Object doRun(WeChatMpDelDraftParams pluginParams) {
    return WeChatMpApiUtil.delDraft(pluginParams.getAccessToken(), pluginParams.getMediaId());
  }
}