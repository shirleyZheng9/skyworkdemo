package com.iwhalecloud.bote.service.plugin.runner;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.util.WeChatMpApiUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.params.WeChatMpQryPublishInfoParams;
import java.util.Arrays;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 微信公众号查询已发布文章状态
 *
 * @author fan.cong
 * @since 2025-08-18
 */
@Component
public class WeChatMpQryPublishInfoPlugin extends AbstractPlugin<WeChatMpQryPublishInfoParams> {

  public WeChatMpQryPublishInfoPlugin() {
    super(WeChatMpQryPublishInfoParams.class);
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGN_CODE_WE_CHAT_MP_QRY_PUBLISH_INFO;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    return ParameterSpec.newRoot(
      Arrays.asList(ParameterSpec.newProperty("articleId", "发布文章articleId", AttrDataType.STRING),
        ParameterSpec.newProperty("accessToken", "公众号access_token", AttrDataType.STRING)));
  }

  @Override
  public ParameterSpec createResponseParameter() {
    return ParameterSpec.newRoot(Arrays.asList(ParameterSpec.newProperty("message", "执行信息", AttrDataType.STRING),
      ParameterSpec.newProperty("articleId", "发布文章articleId", AttrDataType.STRING),
      ParameterSpec.newProperty("newsItem", "图文信息集合", AttrDataType.ARRAY)));
  }

  @Override
  public void validateParams(WeChatMpQryPublishInfoParams params) {
    Assert.notNull(params.getArticleId(), "公众号发布文章articleId不能为空");
    Assert.notNull(params.getAccessToken(), "公众号access_token不能为空");
  }

  @Override
  public Object doRun(WeChatMpQryPublishInfoParams pluginParams) {
    return WeChatMpApiUtil.qryPublishInfo(pluginParams.getAccessToken(), pluginParams.getArticleId());
  }
}