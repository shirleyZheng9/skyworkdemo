package com.iwhalecloud.bote.entity.agent;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 会话状态
 *
 * @author bianjp
 * @since 2026-04-13
 */
@Getter
@Setter
@ToString
public class SessionStateEntity {
  /** ID */
  private Long id;
  /** 会话 ID */
  private Long sessionId;
  /** 作用域 */
  private String stateScope;
  /** 数据(JSON) */
  private String stateData;
  /** 创建时间 */
  private Date createdTime;
  /** 更新时间 */
  private Date updatedTime;
}
