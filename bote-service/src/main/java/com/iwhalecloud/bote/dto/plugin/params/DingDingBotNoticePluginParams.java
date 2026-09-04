package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.dto.plugin.AbstractPluginParams;
import com.iwhalecloud.bote.dto.plugin.message.dingding.AbstractDingDingMessage;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 钉钉机器人通知插件
 *
 * @author qian.sisheng
 * @since 2025-04-10
 */
@Getter
@Setter
@ToString
public class DingDingBotNoticePluginParams extends AbstractPluginParams {
  /** webhook地址accessToken */
  private String accessToken;
  /** 密钥 */
  private String secret;
  /** 被@的人 */
  private At at;
  /** 消息内容 */
  private AbstractDingDingMessage message;

  public DingDingBotNoticePluginParams() {
    super(PluginConsts.PLUGIN_CODE_DING_DING_BOT_NOTICE);
  }

  @Getter
  @Setter
  @ToString
  public static class At {
    /** 被@的人手机号列表 */
    private List<String> atMobiles;
    /** 被@的人userId列表 */
    private List<String> atUserIds;
    /** 是否@所有人 */
    private Boolean isAtAll;
  }
}
