package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.dto.plugin.AbstractPluginParams;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 微信公众号查询已发布文章信息
 *
 * @author fan.cong
 * @since 2025-08-18
 */
@Getter
@Setter
@ToString
public class WeChatMpQryPublishInfoParams extends AbstractPluginParams {
  /**
   * 发布任务articleId
   */
  private String articleId;

  /**
   * 公众号 access_token
   */
  private String accessToken;

  public WeChatMpQryPublishInfoParams() {
    super(PluginConsts.PLUGN_CODE_WE_CHAT_MP_QRY_PUBLISH_INFO);
  }

}
