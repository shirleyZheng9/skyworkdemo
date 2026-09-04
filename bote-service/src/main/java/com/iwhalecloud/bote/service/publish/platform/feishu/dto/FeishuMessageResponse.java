package com.iwhalecloud.bote.service.publish.platform.feishu.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 飞书消息响应抽象类
 * 替代Map<String, Object>，提供类型安全的消息响应
 *
 * @author system
 * @since 2025-01-09
 */
@Getter
@Setter
@ToString
public abstract class FeishuMessageResponse {
  /** 消息类型 */
  protected String msgType;
  /** 接收者ID */
  protected String receiveId;
  /** 接收者类型 */
  protected String receiveIdType;
  /** 会话ID */
  protected String chatId;

  /**
   * 创建文本消息响应
   */
  public static TextResponse createTextResponse(String content) {
    return new TextResponse(content);
  }

  /**
   * 创建富文本消息响应
   */
  public static RichTextResponse createRichTextResponse(String content) {
    return new RichTextResponse(content);
  }

  /**
   * 创建卡片消息响应
   */
  public static CardResponse createCardResponse(String content) {
    return new CardResponse(content);
  }

  /**
   * 创建图片消息响应
   */
  public static ImageResponse createImageResponse(String imageKey) {
    return new ImageResponse(imageKey);
  }

  /**
   * 文本消息响应
   */
  @Getter
  @Setter
  @ToString
  public static class TextResponse extends FeishuMessageResponse {
    private TextContent text;

    public TextResponse(String content) {
      this.msgType = "text";
      this.text = new TextContent(content);
    }

    @Getter
    @Setter
    @ToString
    @AllArgsConstructor
    public static class TextContent {
      private String text;
    }
  }

  /**
   * 富文本消息响应
   */
  @Getter
  @Setter
  @ToString
  public static class RichTextResponse extends FeishuMessageResponse {
    private String post;

    public RichTextResponse(String content) {
      this.msgType = "post";
      this.post = content;
    }
  }

  /**
   * 卡片消息响应
   */
  @Getter
  @Setter
  @ToString
  public static class CardResponse extends FeishuMessageResponse {
    private String interactive;

    public CardResponse(String content) {
      this.msgType = "interactive";
      this.interactive = content;
    }
  }

  /**
   * 图片消息响应
   */
  @Getter
  @Setter
  @ToString
  public static class ImageResponse extends FeishuMessageResponse {
    private ImageContent image;

    public ImageResponse(String imageKey) {
      this.msgType = "image";
      this.image = new ImageContent(imageKey);
    }

    @Getter
    @Setter
    @ToString
    @AllArgsConstructor
    public static class ImageContent {
      private String imageKey;
    }
  }
}
