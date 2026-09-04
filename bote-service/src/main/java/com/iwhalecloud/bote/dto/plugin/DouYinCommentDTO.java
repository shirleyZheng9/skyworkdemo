package com.iwhalecloud.bote.dto.plugin;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 抖音评论
 *
 * @author qian.sisheng
 * @since 2025-08-07
 */
@Getter
@Setter
@ToString
public class DouYinCommentDTO {
  /** 评论内容 */
  private String text;
  /** 评论IP地址 */
  private String ipLabel;
}
