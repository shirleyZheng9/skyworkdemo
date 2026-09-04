package com.iwhalecloud.bote.dto.plugin;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 哔哩哔哩热门列表
 *
 * @author qian.sisheng
 * @since 2025-07-24
 */
@Getter
@Setter
@ToString
public class BiliBiliHotListDTO {
  /** 标题 */
  private String title;
  /** 链接 */
  private String url;
  /** 热度 */
  private Long heat;
}
