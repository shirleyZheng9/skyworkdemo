package com.iwhalecloud.bote.dto.plugin;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 小红书笔记评论
 *
 * @author qian.sisheng
 * @since 2025-07-25
 */
@Getter
@Setter
@ToString
public class RedNoteCommentDTO {
  /** 创建时间 */
  private Long createTime;
  /** 评论内容 */
  private String content;
  /** 点赞数 */
  private String likeCount;
  /** 子评论 */
  private List<RedNoteCommentDTO> subComments;
}
