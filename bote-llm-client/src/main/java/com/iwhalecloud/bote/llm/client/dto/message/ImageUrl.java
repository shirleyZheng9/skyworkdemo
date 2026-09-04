package com.iwhalecloud.bote.llm.client.dto.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.iwhalecloud.bote.llm.client.consts.ImageDetail;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 图片信息
 *
 * @author bianjp
 * @since 2024-08-01
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
public class ImageUrl {
  /** 图片地址，或图片的 data URI 格式("data:content/type;base64," + base64 编码的图片内容) */
  private String url;
  /** 图片细节级别，默认为 auto */
  private ImageDetail detail;

  public ImageUrl() {
  }

  public ImageUrl(String url) {
    this.url = url;
  }

  public ImageUrl(String url, ImageDetail detail) {
    this.url = url;
    this.detail = detail;
  }
}
