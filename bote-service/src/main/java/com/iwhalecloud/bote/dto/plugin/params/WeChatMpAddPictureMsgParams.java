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
 * 微信公众号图片消息参数
 *
 * @author fan.cong
 * @since 2025-08-18
 */
@Setter
@Getter
public class WeChatMpAddPictureMsgParams extends AbstractPluginParams {

  public WeChatMpAddPictureMsgParams() {
    super(PluginConsts.PLUGIN_CODE_WE_CHAT_MP_ADD_PICTURE_MSG);
  }

  /**
   * 图片类型
   */
  private String articleType;

  /**
   * 标题
   */
  private String title;

  /**
   * 内容
   */
  private String content;

  /**
   * 是否打开评论
   */
  private Integer needOpenComment;

  /**
   * 是否仅粉丝可评论
   */
  private Integer onlyFansCanComment;

  /**
   * 公众号 access_token
   */
  private String accessToken;

  /**
   * 图片信息
   */
  private ImageInfo imageInfo;

  public Map<String, Object> toWeChatMpMap() {
    Map<String, Object> map = new HashMap<>();
    map.put("article_type", articleType);
    map.put("title", title);
    map.put("content", content);
    map.put("need_open_comment", needOpenComment);
    map.put("only_fans_can_comment", onlyFansCanComment);
    map.put("image_info", imageInfo != null ? imageInfo.toWeChatMpMap() : null);
    return map;
  }

  /**
   * 图片信息实体
   */
  @Setter
  @Getter
  public static class ImageInfo {
    /**
     * 图片列表
     */
    private List<ImageItem> imageList;

    public Map<String, Object> toWeChatMpMap() {
      Map<String, Object> map = new HashMap<>();
      if (imageList != null) {
        List<Map<String, Object>> imageListMap = new ArrayList<>();
        for (ImageItem item : imageList) {
          imageListMap.add(item.toWeChatMpMap());
        }
        map.put("image_list", imageListMap);
      }
      return map;
    }

    /**
     * 图片项
     */
    @Setter
    @Getter
    public static class ImageItem {

      /**
       * 图片 media_id
       */
      private String imageMediaId;

      public Map<String, Object> toWeChatMpMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("image_media_id", imageMediaId);
        return map;
      }
    }
  }

}
