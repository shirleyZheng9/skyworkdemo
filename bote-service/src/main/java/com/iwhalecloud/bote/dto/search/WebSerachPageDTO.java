package com.iwhalecloud.bote.dto.search;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 联网搜索的页面信息
 *
 * @author chen.linfa
 * @since 2025-03-13
 */
@Getter
@Setter
@ToString
public class WebSerachPageDTO {
  /** 主键 */
  private String id;
  /** 标题 */
  private String name;
  /** 访问链接 */
  private String url;
  /** 内容总结 */
  private String summary;
}
