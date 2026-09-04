package com.iwhalecloud.bote.service.plugin.runner;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.util.WeChatMpApiUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.params.WeChatMpDelPublishParams;
import java.util.Arrays;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 微信公众号删除发布的文章
 *
 * @author fan.cong
 * @since 2025-08-18
 */
@Component
public class WeChatMpDelPublishPlugin extends AbstractPlugin<WeChatMpDelPublishParams> {

  public WeChatMpDelPublishPlugin() {
    super(WeChatMpDelPublishParams.class);
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_WE_CHAT_MP_DEL_PUBLISH;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    return ParameterSpec.newRoot(Arrays.asList(ParameterSpec.newProperty("articleId", "文章ID", AttrDataType.STRING),
      ParameterSpec.newProperty("accessToken", "公众号access_token", AttrDataType.STRING)));
  }

  @Override
  public ParameterSpec createResponseParameter() {
    return ParameterSpec.newRoot(Arrays.asList(ParameterSpec.newProperty("message", "执行信息", AttrDataType.STRING),
      ParameterSpec.newProperty("articleId", "公众号文章Id", AttrDataType.STRING)));
  }

  @Override
  public void validateParams(WeChatMpDelPublishParams params) {
    Assert.notNull(params.getArticleId(), "文章ID不能为空");
    Assert.notNull(params.getAccessToken(), "公众号access_token不能为空");
  }

  @Override
  public Object doRun(WeChatMpDelPublishParams pluginParams) {
    return WeChatMpApiUtil.delPublish(pluginParams.getAccessToken(), pluginParams.getArticleId());
  }
}