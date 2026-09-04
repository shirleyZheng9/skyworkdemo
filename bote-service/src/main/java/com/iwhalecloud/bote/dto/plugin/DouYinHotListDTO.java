package com.iwhalecloud.bote.dto.plugin;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 抖音热搜榜 DTO
 *
 * @author qian.sisheng
 * @since 2025-07-17
 */
@Getter
@Setter
@ToString
public class DouYinHotListDTO {
  /** 标题 */
  private String title;
  /** 链接 */
  private String url;
  /** 热度 */
  private String heat;
}
