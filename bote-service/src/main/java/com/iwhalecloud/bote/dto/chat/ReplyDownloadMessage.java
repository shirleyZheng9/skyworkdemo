package com.iwhalecloud.bote.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文件下载信息
 *
 * <p>SSE 消息，用于通知前端某条回复支持文件下载</p>
 *
 * @author bianjp
 * @since 2025-04-27
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
public class ReplyDownloadMessage {
  /** 文件类型 */
  private String fileType;
}
