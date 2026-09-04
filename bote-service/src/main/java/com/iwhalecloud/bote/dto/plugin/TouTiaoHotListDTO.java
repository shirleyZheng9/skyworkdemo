package com.iwhalecloud.bote.dto.plugin;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 今日头条热搜
 *
 * @author qian.sisheng
 * @since 2025-07-23
 */
@Getter
@Setter
@ToString
public class TouTiaoHotListDTO {
  /** 标题 */
  private String title;
  /** 链接 */
  private String url;
  /** 热度 */
  private String heat;
}
