package com.iwhalecloud.bote.service.asr.realtime.provider;

import com.iwhalecloud.bote.dto.asr.RealtimeAsrResultDTO;
import com.iwhalecloud.bote.service.asr.realtime.RealtimeAsrHandler;
import com.iwhalecloud.bote.service.asr.realtime.IRealtimeAsrProvider;
import com.iwhalecloud.bote.service.asr.realtime.handler.TakeAiRealtimeAsrHandler;
import java.util.function.Consumer;
import org.springframework.stereotype.Component;

/**
 * TakeAi 实时语音识别服务提供者
 *
 * @author qian.sisheng
 * @since 2026-01-14
 */
@Component
public class TakeAiRealtimeAsrProvider implements IRealtimeAsrProvider {

  @Override
  public String getProviderName() {
    return "takeAi";
  }

  @Override
  public RealtimeAsrHandler createRealtimeAsrHandler(Consumer<RealtimeAsrResultDTO> onMessage, Consumer<Throwable> onError) {
    return new TakeAiRealtimeAsrHandler(onMessage, onError);
  }
}
