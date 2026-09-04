package com.iwhalecloud.bote.service.plugin.runner;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.util.WeChatMpApiUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.params.WeChatMpAddPictureMsgParams;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 微信公众号新增图片草稿
 *
 * @author fan.cong
 * @since 2025-08-18
 */
@Component
public class WeChatMpAddPictureMsgPlugin extends AbstractPlugin<WeChatMpAddPictureMsgParams> {

  public WeChatMpAddPictureMsgPlugin() {
    super(WeChatMpAddPictureMsgParams.class);
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_WE_CHAT_MP_ADD_PICTURE_MSG;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    return ParameterSpec.newRoot(Arrays.asList(ParameterSpec.newProperty("title", "标题", AttrDataType.STRING),
      ParameterSpec.newProperty("content", "内容", AttrDataType.STRING),
      ParameterSpec.newProperty("needOpenComment", "是否打开评论", AttrDataType.INTEGER),
      ParameterSpec.newProperty("onlyFansCanComment", "是否仅粉丝可评论", AttrDataType.INTEGER),
      ParameterSpec.newProperty("imageInfo", "图片信息结构", AttrDataType.OBJECT),
      ParameterSpec.newProperty("accessToken", "公众号access_token", AttrDataType.STRING)));
  }

  @Override
  public ParameterSpec createResponseParameter() {
    return ParameterSpec.newRoot(Arrays.asList(ParameterSpec.newProperty("message", "执行信息", AttrDataType.STRING),
      ParameterSpec.newProperty("mediaId", "公众号草稿箱草稿Id", AttrDataType.STRING)));
  }

  @Override
  public void validateParams(WeChatMpAddPictureMsgParams params) {
    Assert.notNull(params.getAccessToken(), "公众号access_token不能为空");
    Assert.notNull(params.getTitle(), "标题不能为空");
    Assert.notNull(params.getContent(), "内容不能为空");
    Assert.notNull(params.getImageInfo(), "图片信息不能为空");
  }

  @Override
  public Object doRun(WeChatMpAddPictureMsgParams pluginParams) {
    // 循环给articles中的articleType赋值
    pluginParams.setArticleType("newspic");
    pluginParams.setArticleType("newspic");
    Map<String, Object> map = new HashMap<>();
    map.put("articles", Collections.singletonList(pluginParams.toWeChatMpMap()));

    return WeChatMpApiUtil.addDraft(pluginParams.getAccessToken(), map);
  }

}