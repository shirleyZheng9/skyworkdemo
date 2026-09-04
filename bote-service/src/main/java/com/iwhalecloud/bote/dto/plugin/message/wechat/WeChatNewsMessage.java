package com.iwhalecloud.bote.dto.plugin.message.wechat;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 微信图文消息
 *
 * @author qian.sisheng
 * @since 2025-04-14
 */
@Getter
@Setter
@ToString
public class WeChatNewsMessage extends AbstractWeChatMessage {

  /** 图文 */
  private News news;

  public WeChatNewsMessage() {
    super(PluginConsts.MESSAGE_TYPE_NEWS);
  }

  @Getter
  @Setter
  @ToString
  public static class News {
    /** 图文消息，一个图文消息支持1到8条图文 */
    private List<Article> articles;
  }

  @Getter
  @Setter
  @ToString
  public static class Article {
    /** 标题，不超过128个字节，超过会自动截断 */
    private String title;
    /** 描述，不超过512个字节，超过会自动截断 */
    private String description;
    /** 点击后跳转的链接 */
    private String url;
    /** 图文消息的图片链接，支持JPG、PNG格式，较好的效果为大图 1068*455，小图150*150 */
    private String picurl;
  }
}
