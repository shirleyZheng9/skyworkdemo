package com.iwhalecloud.bote.service.plugin.runner;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.util.WeChatMpApiUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.params.WeChatMpPublishDraftParams;
import java.util.Arrays;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 微信公众号发布草稿
 *
 * @author fan.cong
 * @since 2025-08-18
 */
@Component
public class WeChatMpPublishDraftPlugin extends AbstractPlugin<WeChatMpPublishDraftParams> {

  public WeChatMpPublishDraftPlugin() {
    super(WeChatMpPublishDraftParams.class);
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_WE_CHAT_MP_PUBLISH_DRAFT;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    return ParameterSpec.newRoot(Arrays.asList(ParameterSpec.newProperty("mediaId", "草稿ID", AttrDataType.STRING),
      ParameterSpec.newProperty("accessToken", "公众号access_token", AttrDataType.STRING)));
  }

  @Override
  public ParameterSpec createResponseParameter() {
    return ParameterSpec.newRoot(Arrays.asList(ParameterSpec.newProperty("message", "执行信息", AttrDataType.STRING),
      ParameterSpec.newProperty("publishId", "发布任务的id", AttrDataType.STRING),
      ParameterSpec.newProperty("msgDataId", "消息的数据id", AttrDataType.STRING),
      ParameterSpec.newProperty("mediaId", "发布的草稿id", AttrDataType.STRING)));
  }

  @Override
  public void validateParams(WeChatMpPublishDraftParams params) {
    Assert.notNull(params.getMediaId(), "草稿ID不能为空");
    Assert.notNull(params.getAccessToken(), "公众号access_token不能为空");
  }

  @Override
  public Object doRun(WeChatMpPublishDraftParams pluginParams) {
    return WeChatMpApiUtil.publishDraft(pluginParams.getAccessToken(), pluginParams.getMediaId());
  }
}