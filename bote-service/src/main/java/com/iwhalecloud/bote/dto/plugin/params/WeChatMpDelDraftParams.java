package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.dto.plugin.AbstractPluginParams;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 微信公众号删除草稿
 *
 * @author fan.cong
 * @since 2025-08-18
 */
@Getter
@Setter
@ToString
public class WeChatMpDelDraftParams extends AbstractPluginParams {
  /**
   * 草稿ID
   */
  private String mediaId;

  /**
   * 公众号 access_token
   */
  private String accessToken;

  public WeChatMpDelDraftParams() {
    super(PluginConsts.PLUGIN_CODE_WE_CHAT_MP_DEL_DRAFT);
  }

}
