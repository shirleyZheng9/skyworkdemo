package com.iwhalecloud.bote.dto.plugin;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 知乎热榜搜索结果
 *
 * @author qian.sisheng
 * @since 2025-07-23
 */
@Getter
@Setter
@ToString
public class ZhiHuHotSearchDTO {
  /** 标题 */
  private String title;
  /** 链接 */
  private String url;
  /** 描述 */
  private String desc;
  /** 热度 */
  private String heat;
}
