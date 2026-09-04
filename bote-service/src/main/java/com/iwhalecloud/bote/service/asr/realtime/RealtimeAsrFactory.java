package com.iwhalecloud.bote.service.asr.realtime;

import com.iwhalecloud.bote.dto.asr.RealtimeAsrResultDTO;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.dto.asr.VideoProviderInfoDTO;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 实时语音识别服务工厂
 *
 * @author qian.sisheng
 * @since 2026-01-14
 */
@Component
public class RealtimeAsrFactory {

  private final Map<String, IRealtimeAsrProvider> providers = new ConcurrentHashMap<>();

  @Autowired
  public RealtimeAsrFactory(Map<String, IRealtimeAsrProvider> providerMap) {
    providerMap.forEach((k, v) -> providers.put(v.getProviderName(), v));
  }

  /**
   * 根据提供商名称创建实时语音识别服务
   *
   * @param providerName 提供商名称（如 "takeAi", "panzhi"）
   * @param onMessage 消息回调
   * @param onError 错误回调
   * @return 实时语音识别服务实例
   * @throws UnsupportedOperationException 如果提供商不存在或不支持实时识别
   */
  public RealtimeAsrHandler getRealtimeAsrHandler(String providerName, Consumer<RealtimeAsrResultDTO> onMessage, Consumer<Throwable> onError) {
    IRealtimeAsrProvider provider = providers.get(providerName);
    if (provider == null) {
      throw new BssException("暂不支持实时语音识别或未启用: " + providerName);
    }
    return provider.createRealtimeAsrHandler(onMessage, onError);
  }

  /**
   * 检查是否支持实时识别
   *
   * @return true: 支持, false: 不支持
   */
  public boolean isProviderAvailable() {
    return providers.containsKey(SystemParameter.VIDEO_ORC_TYPE.getValueFromDb());
  }

  /**
   * 获取当前语音识别信息
   */
  public VideoProviderInfoDTO getProviderInfo() {
    String type = SystemParameter.VIDEO_ORC_TYPE.getValueFromDb();
    VideoProviderInfoDTO providerInfo = new VideoProviderInfoDTO();
    providerInfo.setProvider(type);
    providerInfo.setSupportRealtime(isProviderAvailable());
    providerInfo.setRealtimeEnabled("realtime".equals(SystemParameter.VIDEO_RECOGNITION_MODE.getValueFromDb()));
    return providerInfo;
  }
}
