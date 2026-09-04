package com.iwhalecloud.bote.service.asr.realtime;

import com.iwhalecloud.bote.dto.asr.RealtimeAsrResultDTO;
import java.util.function.Consumer;

/**
 * 实时语音识别服务提供者接口
 *
 * @author qian.sisheng
 * @since 2026-01-14
 */
public interface IRealtimeAsrProvider {

  /**
   * 获取名称
   *
   * @return 提供商名称
   */
  String getProviderName();

  /**
   * 创建实时语音识别服务实例
   *
   * @param onMessage 消息回调
   * @param onError   错误回调
   * @return 实时语音识别服务实例
   */
  RealtimeAsrHandler createRealtimeAsrHandler(Consumer<RealtimeAsrResultDTO> onMessage, Consumer<Throwable> onError);
}
