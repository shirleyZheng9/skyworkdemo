package com.iwhalecloud.bote.dto.agent;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.entity.agent.SessionTaskEntity;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 会话任务 DTO
 *
 * @author bianjp
 * @since 2026-04-10
 */
@Getter
@Setter
@ToString(callSuper = true)
public class SessionTaskDTO {
  /** 主键 */
  protected Long id;
  /** 任务 ID（会话内递增） */
  protected Integer taskId;
  /** 任务主题 */
  protected String subject;
  /** 任务描述 */
  protected String description;
  /** 进行时形式 */
  protected String activeForm;
  /** 任务状态 */
  protected String taskStatus;
  /** 阻塞的任务 ID 列表 */
  private List<Integer> blocks;
  /** 依赖的任务 ID 列表 */
  private List<Integer> blockedBy;

  public SessionTaskDTO() {
  }

  public SessionTaskDTO(SessionTaskEntity entity) {
    this.id = entity.getId();
    this.taskId = entity.getTaskId();
    this.subject = entity.getSubject();
    this.description = entity.getDescription();
    this.activeForm = entity.getActiveForm();
    this.taskStatus = entity.getTaskStatus();
    this.blocks = JsonUtil.parseJson(entity.getBlocksJson(), new TypeReference<>() {
    });
    this.blockedBy = JsonUtil.parseJson(entity.getBlockedByJson(), new TypeReference<>() {
    });
  }
}
