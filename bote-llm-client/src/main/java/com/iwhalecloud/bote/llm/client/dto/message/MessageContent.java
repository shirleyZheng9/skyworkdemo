package com.iwhalecloud.bote.llm.client.dto.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.iwhalecloud.bote.llm.client.consts.ContentType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 消息内容
 *
 * @author bianjp
 * @since 2024-08-01
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
@JsonNaming(SnakeCaseStrategy.class)
public class MessageContent {
  /** 类型 */
  private ContentType type;
  /** 文本内容 */
  private String text;
  /** 图片信息 */
  private ImageUrl imageUrl;

  public MessageContent() {
  }

  /**
   * 构造文本消息内容
   */
  public MessageContent(String text) {
    this.type = ContentType.TEXT;
    this.text = text;
  }

  /**
   * 构造图片消息内容
   */
  public MessageContent(ImageUrl imageUrl) {
    this.type = ContentType.IMAGE_URL;
    this.imageUrl = imageUrl;
  }
}
