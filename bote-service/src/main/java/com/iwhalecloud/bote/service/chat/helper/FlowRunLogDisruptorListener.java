package com.iwhalecloud.bote.service.chat.helper;

import com.iwhalecloud.bote.entity.skill.FlowRunLogDTO;
import com.iwhalecloud.bote.mapper.skill.FlowRunLogMapper;
import com.iwhalecloud.bss.litchi.disruptor.DisruptorEventListener;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 流程日志监听器，用于异步记录流程日志
 *
 * @author bianjp
 * @since 2025-03-05
 */
@Component
@RequiredArgsConstructor
public class FlowRunLogDisruptorListener implements DisruptorEventListener<FlowRunLogDTO> {
  private final FlowRunLogMapper flowRunLogMapper;

  @Override
  public void onEvent(FlowRunLogDTO event) {
    flowRunLogMapper.insert(event);
  }
}
