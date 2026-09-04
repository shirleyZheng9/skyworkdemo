package com.iwhalecloud.bote.dto.plugin;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 微博热搜结果
 *
 * @author qian.sisheng
 * @since 2025-07-23
 */
@Getter
@Setter
@ToString
public class WeiBoHotListDTO {
  /** 热度 */
  private Long heat;
  /** 热搜标题 */
  private String title;
  /** 排序 */
  private Integer sort;
}
