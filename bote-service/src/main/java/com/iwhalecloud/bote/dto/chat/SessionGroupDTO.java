package com.iwhalecloud.bote.dto.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 会话分组
 *
 * @author bianjp
 * @since 2025-03-12
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "会话分组")
public class SessionGroupDTO {
  @Schema(description = "今天的会话列表")
  private List<SessionDTO> today;
  @Schema(description = "最近 7 天的会话列表")
  private List<SessionDTO> sevenDaysAgo;
  @Schema(description = "最近 30 天的会话列表")
  private List<SessionDTO> thirtyDaysAgo;
  @Schema(description = "最近 30 天之外的会话列表")
  private List<SessionDTO> thirtyDaysAfter;
}
