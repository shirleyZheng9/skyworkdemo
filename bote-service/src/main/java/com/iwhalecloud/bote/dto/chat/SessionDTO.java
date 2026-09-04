package com.iwhalecloud.bote.dto.chat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.iwhalecloud.bote.entity.chat.SessionEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 会话 DTO
 *
 * @author auto
 * @since 2024-10-28
 */
@Getter
@Setter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
public class SessionDTO extends SessionEntity {
  @Schema(description = "会话中最后一条消息的时间，会话分组使用，不返回给前端", hidden = true)
  @JsonIgnore
  private Date lastMsgTime;
  @Schema(description = "是否平台发布：T:是，F:否")
  private String isPlatformPublish;
}
