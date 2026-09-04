package com.iwhalecloud.bote.dto.chat.query;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.chat.vo.SessionMsgVO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 会话消息
 *
 * @author Admin
 */
@Getter
@Setter
@ToString
@Schema(description = "会话消息")
public class MessageGroupResponse {
  @Schema(description = "历史场景 ID")
  private Long sceneId;
  @Schema(description = "历史场景名称")
  private String sceneName;
  @Schema(description = "历史场景上下文 ID")
  private String contextId;
  @Schema(description = "是否自动启动")
  private Boolean autoStartEnabled;
  @Schema(description = "会话消息列表")
  private List<SessionMsgVO> messages;
  @Schema(description = "会话消息分页信息")
  private PageInfo<SessionMsgVO> messagePage;
}
