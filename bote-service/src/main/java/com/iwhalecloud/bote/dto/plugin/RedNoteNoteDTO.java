package com.iwhalecloud.bote.dto.plugin;

import lombok.Getter;
import lombok.Setter;

/**
 * 小红书笔记
 *
 * @author qian.sisheng
 * @since 2025-07-22
 */
@Getter
@Setter
public class RedNoteNoteDTO {
  /** 笔记url */
  private String url;
  /** 点赞数 */
  private Long likedCount;
  /** 笔记id */
  private String id;
  /** 笔记标题 */
  private String title;
}
