package com.iwhalecloud.bote.dto.plugin;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 哔哩哔哩视频搜索结果
 *
 * @author qian.sisheng
 * @since 2025-07-24
 */
@Getter
@Setter
@ToString
public class BiliBiliVideoSearchDTO {
  /** 视频标题 */
  private String title;
  /** 视频描述 */
  private String description;
  /** 视频链接 */
  private String url;
  /** 播放量 */
  private Long playCount;
  /** 点赞数 */
  private Long likes;
  /** 弹幕数 */
  private Long bulletComments;
  /** 评论数 */
  private Long comments;
  /** 标签 */
  private String tag;
}
