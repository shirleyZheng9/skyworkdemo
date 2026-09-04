package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.dto.plugin.AbstractPluginParams;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/**
 * 微信公众号图文消息参数
 *
 * @author fan.cong
 * @since 2025-08-18
 */
@Setter
@Getter
public class WeChatMpAddGraphicMsgParams extends AbstractPluginParams {
  /**
   * 图文消息结构
   */
  private List<Articles> articles;

  /**
   * 公众号 access_token
   */
  private String accessToken;

  public WeChatMpAddGraphicMsgParams() {
    super(PluginConsts.PLUGIN_CODE_WE_CHAT_MP_ADD_GRAPHIC_MSG);
  }

  /**
   * 转为微信接口需要的 Map 格式（带下划线 key）
   */
  public Map<String, Object> toWeChatMpMap() {
    Map<String, Object> map = new HashMap<>();
    if (articles != null && !articles.isEmpty()) {
      List<Map<String, Object>> articleList = new ArrayList<>();
      for (Articles article : articles) {
        articleList.add(article.toWeChatMpMap());
      }
      map.put("articles", articleList);
    }
    return map;
  }

  @Getter
  @Setter
  public static class Articles {
    /**
     * 图文消息类型
     */
    private String articleType;

    /**
     * 标题
     */
    private String title;

    /**
     * 作者
     */
    private String author;

    /**
     * 图文消息的摘要，仅有单图文消息才有摘要，多图文此处为空。 如果本字段为没有填写，则默认抓取正文前54个字
     */
    private String digest;

    /**
     * 图文消息的具体内容，支持HTML标签，必须少于2万字符，小于1M， 且此处会去除JS,涉及图片url必须来源 "上传图文消息内的图片获取URL"接口获取。
     */
    private String content;

    /**
     * 图文消息的原文地址，即点击“阅读原文”后的URL
     */
    private String contentSourceUrl;

    /**
     * article_type为图文消息（news）时必填， 图文消息的封面图片素材id（必须是永久MediaID）
     */
    private String thumbMediaId;

    /**
     * 是否打开评论，0不打开(默认)，1打开
     */
    private int needOpenComment;

    /**
     * 是否粉丝才可评论，0所有人可评论(默认)，1粉丝才可评论
     */
    private int onlyFansCanComment;

    /**
     * 转为微信接口需要的 Map 格式（带下划线 key）
     */
    public Map<String, Object> toWeChatMpMap() {
      Map<String, Object> map = new HashMap<>();
      map.put("article_type", articleType);
      map.put("title", title);
      map.put("author", author);
      map.put("digest", digest);
      map.put("content", content);
      map.put("content_source_url", contentSourceUrl);
      map.put("thumb_media_id", thumbMediaId);
      map.put("need_open_comment", needOpenComment);
      map.put("only_fans_can_comment", onlyFansCanComment);
      return map;
    }
  }

}

