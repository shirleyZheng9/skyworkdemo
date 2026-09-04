package com.iwhalecloud.bote.dto.chat;

import com.iwhalecloud.bote.entity.chat.SessionMsgFileEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 会话消息附件
 *
 * @author chen.linfa
 * @since 2024-08-19
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "会话消息附件传输对象")
public class SessionMsgFileDTO extends SessionMsgFileEntity {
  @Schema(description = "文件名称")
  private String fileName;
  @Schema(description = "文件大小")
  private Long fileSize;
  @Schema(description = "文件类型")
  private String fileType;
  @Schema(description = "文件 ID 集合")
  private List<Long> fileIds;
  @Schema(description = "文件是否是图片")
  private Boolean isPicture;
}
