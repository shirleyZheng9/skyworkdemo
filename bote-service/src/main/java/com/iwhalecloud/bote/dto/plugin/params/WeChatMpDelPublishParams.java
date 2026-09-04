package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.dto.plugin.AbstractPluginParams;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 微信公众号删除发布的文章
 *
 * @author fan.cong
 * @since 2025-08-18
 */
@Getter
@Setter
@ToString
public class WeChatMpDelPublishParams extends AbstractPluginParams {
  /**
   * 文章 Id
   */
  private String articleId;

  /**
   * 公众号 access_token
   */
  private String accessToken;

  public WeChatMpDelPublishParams() {
    super(PluginConsts.PLUGIN_CODE_WE_CHAT_MP_DEL_PUBLISH);
  }

}
