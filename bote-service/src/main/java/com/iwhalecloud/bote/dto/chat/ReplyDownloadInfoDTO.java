package com.iwhalecloud.bote.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 文件下载信息
 *
 * @author bianjp
 * @since 2025-04-27
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ReplyDownloadInfoDTO {
  /** 文件类型 */
  private String fileType;
  /** 内容 */
  private String content;
  /** 段落分组 */
  private String paragraphGroup;
  /** 段落排序 */
  private Integer paragraphSortby;
  /** 事务 ID */
  private Long transactionId;
}
