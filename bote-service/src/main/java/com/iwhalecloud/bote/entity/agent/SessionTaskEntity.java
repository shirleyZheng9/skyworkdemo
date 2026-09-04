package com.iwhalecloud.bote.entity.agent;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 会话任务 Entity
 *
 * @author bianjp
 * @since 2026-04-10
 */
@Getter
@Setter
@ToString(callSuper = true)
public class SessionTaskEntity {
  /** 主键 */
  private Long id;
  /** 会话 ID */
  private Long sessionId;
  /** 任务 ID（会话内递增） */
  private Integer taskId;
  /** 任务主题 */
  private String subject;
  /** 任务描述 */
  private String description;
  /** 进行时形式 */
  private String activeForm;
  /** 任务状态 */
  private String taskStatus;
  /** 阻塞的任务 ID 列表（JSON） */
  private String blocksJson;
  /** 依赖的任务 ID 列表（JSON） */
  private String blockedByJson;
  /** 逻辑删除标记（0/1） */
  private Integer isDeleted;
  /** 创建时间 */
  private Date createTime;
  /** 更新时间 */
  private Date updateTime;
}
