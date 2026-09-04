package com.iwhalecloud.bote.adapter.panzhi.service;

import com.iwhalecloud.bote.dto.asr.RealtimeAsrResultDTO;
import com.iwhalecloud.bote.service.asr.realtime.IRealtimeAsrProvider;
import com.iwhalecloud.bote.service.asr.realtime.RealtimeAsrHandler;
import java.util.function.Consumer;
import org.springframework.stereotype.Component;

/**
 * 磐智实时语音识别服务提供者
 *
 * @author qian.sisheng
 * @since 2026-01-14
 */
@Component
public class PanzhiRealtimeAsrProvider implements IRealtimeAsrProvider {

  @Override
  public String getProviderName() {
    return "panzhi";
  }

  @Override
  public RealtimeAsrHandler createRealtimeAsrHandler(Consumer<RealtimeAsrResultDTO> onMessage, Consumer<Throwable> onError) {
    return new PanzhiRealtimeAsrHandler(onMessage, onError);
  }
}
