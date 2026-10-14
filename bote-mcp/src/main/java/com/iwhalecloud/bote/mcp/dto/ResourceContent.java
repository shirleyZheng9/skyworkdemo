package com.iwhalecloud.bote.mcp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 资源内容：文本资源(text)或二进制资源(blob)，二者互斥
 *
 * @author zhusenliang
 * @since 2026-09-08
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
public class ResourceContent {
  /** 资源 URI */
  private String uri;
  /** 媒体类型 */
  private String mimeType;
  /** 文本内容，与 blob 互斥 */
  private String text;
  /** 二进制内容(base64)，与 text 互斥 */
  private String blob;
}
