package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.dto.plugin.AbstractPluginParams;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 微信公众号查询已发布文章状态
 *
 * @author fan.cong
 * @since 2025-08-18
 */
@Getter
@Setter
@ToString
public class WeChatMpQryPublishStateParams extends AbstractPluginParams {
  /**
   * 发布任务的id
   */
  private String publishId;

  /**
   * 公众号 access_token
   */
  private String accessToken;

  public WeChatMpQryPublishStateParams() {
    super(PluginConsts.PLUGIN_CODE_WE_CHAT_MP_QRY_PUBLISH_STATE);
  }

}
