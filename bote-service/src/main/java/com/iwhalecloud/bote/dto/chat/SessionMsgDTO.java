package com.iwhalecloud.bote.dto.chat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.util.MemoryContentUtil;
import com.iwhalecloud.bote.dto.SystemReminder;
import com.iwhalecloud.bote.entity.chat.SessionMsgEntity;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 会话消息 DTO
 *
 * @author auto
 * @since 2024-10-28
 */
@Getter
@Setter
@ToString(callSuper = true)
public class SessionMsgDTO extends SessionMsgEntity {
  @Schema(description = "会话消息文本")
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

  @Schema(description = "文件 ID 列表，后端保存消息附件时使用", hidden = true)
  @JsonIgnore
  private List<Long> fileIds;

  /**
   * 添加系统提醒
   */
  public void addReminders(List<SystemReminder> reminders) {
    // 忽略不需要持久化的系统提醒
    List<SystemReminder> persistentReminders = reminders.stream().filter(r -> r.type().isPersistent()).toList();
    if (persistentReminders.isEmpty()) {
      return;
    }
    SessionMsgExtParamsDTO params = JsonUtil.parseJson(extParams, SessionMsgExtParamsDTO.class);
    if (params == null) {
      params = new SessionMsgExtParamsDTO();
    }
    params.setReminders(reminders);
    extParams = JsonUtil.toJsonString(params);
  }

  /**
   * 设置记忆
   */
  public void setMemory(boolean memorized, Object memoryContent) {
    if (memorized) {
      this.memorized = BaseConsts.TRUE;
      this.memoryContent = MemoryContentUtil.toString(memoryContent);
    }
    else {
      this.memorized = BaseConsts.FALSE;
    }
  }
}
