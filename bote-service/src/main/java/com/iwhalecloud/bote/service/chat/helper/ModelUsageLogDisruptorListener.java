package com.iwhalecloud.bote.service.chat.helper;

import com.iwhalecloud.bote.entity.model.ModelUsageLogEntity;
import com.iwhalecloud.bote.mapper.model.ModelUsageLogMapper;
import com.iwhalecloud.bss.litchi.disruptor.DisruptorEventListener;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 大模型使用量日志监听器，用于异步记录日志
 *
 * @author chen.linfa
 * @since 2026-04-07
 */
@Component
@RequiredArgsConstructor
public class ModelUsageLogDisruptorListener implements DisruptorEventListener<ModelUsageLogEntity> {
  private final ModelUsageLogMapper modelUsageLogMapper;

  @Override
  public void onEvent(ModelUsageLogEntity event) {
    modelUsageLogMapper.insert(event);
  }
}
