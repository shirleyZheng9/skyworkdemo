package com.iwhalecloud.bote.service.plugin.runner;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.util.WeChatMpApiUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.params.WeChatMpAddGraphicMsgParams;
import java.util.Arrays;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 微信公众号新增图文草稿
 *
 * @author fan.cong
 * @since 2025-08-18
 */
@Component
public class WeChatMpAddGraphicMsgPlugin extends AbstractPlugin<WeChatMpAddGraphicMsgParams> {

  public WeChatMpAddGraphicMsgPlugin() {
    super(WeChatMpAddGraphicMsgParams.class);
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_WE_CHAT_MP_ADD_GRAPHIC_MSG;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    return ParameterSpec.newRoot(
      Arrays.asList(ParameterSpec.newProperty("articles", "图文消息结构", AttrDataType.ARRAY),
        ParameterSpec.newProperty("accessToken", "公众号access_token", AttrDataType.STRING)));
  }

  @Override
  public ParameterSpec createResponseParameter() {
    return ParameterSpec.newRoot(Arrays.asList(ParameterSpec.newProperty("message", "执行信息", AttrDataType.STRING),
      ParameterSpec.newProperty("mediaId", "公众号草稿箱草稿Id", AttrDataType.STRING)));
  }

  @Override
  public void validateParams(WeChatMpAddGraphicMsgParams params) {
    Assert.notNull(params.getArticles(), "图文消息结构不能为空");
    Assert.notNull(params.getAccessToken(), "公众号access_token不能为空");
  }

  @Override
  public Object doRun(WeChatMpAddGraphicMsgParams pluginParams) {
    // 循环给articles中的articleType赋值
    for (WeChatMpAddGraphicMsgParams.Articles article : pluginParams.getArticles()) {
      article.setArticleType("news");
    }
    return WeChatMpApiUtil.addDraft(pluginParams.getAccessToken(), pluginParams.toWeChatMpMap());
  }

}