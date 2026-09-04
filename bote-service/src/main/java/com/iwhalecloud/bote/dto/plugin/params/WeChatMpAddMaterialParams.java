package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.dto.plugin.AbstractPluginParams;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 微信公众号上传素材
 *
 * @author fan.cong
 * @since 2025-08-18
 */
@Getter
@Setter
@ToString
public class WeChatMpAddMaterialParams extends AbstractPluginParams {
  /**
   * 文件ID
   */
  private Long fileId;

  /**
   * 公众号 access_token
   */
  private String accessToken;

  /**
   * 文件描述
   */
  private Descriptor description;

  public WeChatMpAddMaterialParams() {
    super(PluginConsts.PLUGIN_CODE_WE_CHAT_MP_ADD_MATERIAL);
  }

  @Getter
  @Setter
  public static class Descriptor {

    /**
     * 视频素材描述标题
     */
    private String title;

    /**
     * 视频素材描述简介
     */
    private String introduction;

  }
}
