package com.iwhalecloud.bote.service.asr.offline;

import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * 语音识别工厂类
 *
 * @author qian.sisheng
 * @since 2025-10-27
 */
@Component
public class AsrRecognitionFactory {

  /** 语音识别适配器列表 */
  private final List<IAsrRecognitionAdapter> adapters = new ArrayList<>();

  /** 语音识别适配器映射，key为类型，value为适配器实例 */
  private final Map<String, IAsrRecognitionAdapter> adapterMap = new ConcurrentHashMap<>();

  @PostConstruct
  public void init() {
    // 从Spring容器中获取所有语音识别适配器
    adapters.addAll(SpringUtil.getBeansOfType(IAsrRecognitionAdapter.class).values());

    for (IAsrRecognitionAdapter adapter : adapters) {
      adapterMap.put(adapter.getVideoRecognizeType(), adapter);
    }
  }

  /**
   * 获取语音识别适配器
   *
   * @return 语音识别适配器实例
   */
  public IAsrRecognitionAdapter getVideoRecognitionAdapter() {
    if (adapters.isEmpty()) {
      throw new BssException("没有可用的语音识别适配器");
    }
    // 获取语音识别类型
    String type = SystemParameter.VIDEO_ORC_TYPE.getValueFromDb();
    IAsrRecognitionAdapter adapter = adapterMap.get(type);
    if (adapter == null) {
      throw new BssException("未知的语音转写类型，type=" + type);
    }
    return adapter;
  }
}
