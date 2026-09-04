package com.iwhalecloud.bote.entity.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 会话消息内容 Entity
 *
 * @author auto
 * @since 2024-10-28
 */
@Getter
@Setter
@ToString
@Schema(description = "会话消息内容")
@Table(name = "bt_bot_session_msg_text")
public class SessionMsgTextEntity {
  @Schema(description = "消息 ID")
  private Long msgId;
  @Schema(description = "消息内容")
  private String msgText;
  @Schema(description = "记忆内容")
  private String memoryContent;
  @Schema(description = "扩展参数")
  private String extParams;
  @Schema(description = "文件下载类型")
  private String downloadType;
  @Schema(description = "文件下载内容")
  private String downloadContent;
  @Schema(description = "输出内容格式")
  private String contentType;
  @Schema(description = "段落格式归属的文档")
  private String paragraphGroup;
  @Schema(description = "段落格式的序号")
  private Integer paragraphSortby;
  @Schema(description = "修改时间")
  protected Date updatedTime;
}
