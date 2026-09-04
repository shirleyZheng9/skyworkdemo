package com.iwhalecloud.bote.service.asr.realtime.provider;

import com.iwhalecloud.bote.dto.asr.RealtimeAsrResultDTO;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.util.GroovyUtil;
import com.iwhalecloud.bote.service.asr.realtime.RealtimeAsrHandler;
import com.iwhalecloud.bote.service.asr.realtime.IRealtimeAsrProvider;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.function.Consumer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 自定义实时语音识别提供者
 *
 * @author qian.sisheng
 * @since 2026-01-14
 */
@Component
public class CustomRealtimeAsrProvider implements IRealtimeAsrProvider {
  private static final Logger logger = LoggerFactory.getLogger(CustomRealtimeAsrProvider.class);
  @Override
  public String getProviderName() {
    return "custom";
  }

  @Override
  @SuppressWarnings("PMD.AvoidInstanceofChecksInCatchClause")
  public RealtimeAsrHandler createRealtimeAsrHandler(Consumer<RealtimeAsrResultDTO> onMessage, Consumer<Throwable> onError) {
    String code = SystemParameter.CUSTOM_VIDEO_RECOGNITION_SCRIPT.getValueFromDb();
    if (StringUtils.isEmpty(code)) {
      throw new BssException("未配置自定义语音识别脚本");
    }
    try {
      // 调用脚本中的 createRealtimeAsrHandler 方法
      // 脚本需返回实现了 RealtimeAsrHandler 接口的对象
      Object realtimeAsrHandler = GroovyUtil.invoke(code, null, "createRealtimeAsrHandler", onMessage, onError);
      if (realtimeAsrHandler instanceof RealtimeAsrHandler) {
        return (RealtimeAsrHandler) realtimeAsrHandler;
      }
      throw new BssException("自定义脚本 createRealtimeAsrHandler 方法未返回有效的 RealtimeAsrHandler 对象");
    }
    catch (Exception e) {
      logger.error("Failed to create realtime session by script: {}", code, e);
      if (e instanceof BssException && e.getMessage().contains("createRealtimeAsrHandler")) {
        throw new BssException("自定义脚本未实现 createRealtimeAsrHandler 方法，不支持实时识别", e);
      }
      throw e;
    }
  }
}
