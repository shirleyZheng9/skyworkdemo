package com.iwhalecloud.bote.service.plugin.runner;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.util.WeChatMpApiUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.params.WeChatMpQryPublishStateParams;
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
public class WeChatMpQryPublishStatePlugin extends AbstractPlugin<WeChatMpQryPublishStateParams> {

  public WeChatMpQryPublishStatePlugin() {
    super(WeChatMpQryPublishStateParams.class);
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_WE_CHAT_MP_QRY_PUBLISH_STATE;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    return ParameterSpec.newRoot(Arrays.asList(ParameterSpec.newProperty("publishId", "发布ID", AttrDataType.STRING),
      ParameterSpec.newProperty("accessToken", "公众号access_token", AttrDataType.STRING)));
  }

  @Override
  public ParameterSpec createResponseParameter() {
    return ParameterSpec.newRoot(Arrays.asList(ParameterSpec.newProperty("message", "执行信息", AttrDataType.STRING),
      ParameterSpec.newProperty("publishId", "发布ID", AttrDataType.STRING),
      ParameterSpec.newProperty("publishStatus", "发布状态", AttrDataType.STRING),
      ParameterSpec.newProperty("articleId", "文章ID", AttrDataType.STRING),
      ParameterSpec.newProperty("articleDetail", "文章详情", AttrDataType.OBJECT)));
  }

  @Override
  public void validateParams(WeChatMpQryPublishStateParams params) {
    Assert.notNull(params.getPublishId(), "发布ID不能为空");
    Assert.notNull(params.getAccessToken(), "公众号access_token不能为空");
  }

  @Override
  public Object doRun(WeChatMpQryPublishStateParams pluginParams) {
    return WeChatMpApiUtil.qryPublishState(pluginParams.getAccessToken(), pluginParams.getPublishId());
  }
}