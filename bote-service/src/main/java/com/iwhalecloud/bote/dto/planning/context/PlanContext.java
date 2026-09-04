package com.iwhalecloud.bote.dto.planning.context;

import com.iwhalecloud.bote.dto.planning.PlanRecordDTO;
import com.iwhalecloud.bote.service.chat.context.ChatContext;
import lombok.Getter;
import lombok.Setter;

/**
 * 计划上下文
 *
 * @author chen.linfa
 * @since 2025-07-11
 */
@Getter
@Setter
public class PlanContext {
  /** 记录 */
  private PlanRecordDTO record;
  /** 会话上下文 */
  private ChatContext chatContext;
}
