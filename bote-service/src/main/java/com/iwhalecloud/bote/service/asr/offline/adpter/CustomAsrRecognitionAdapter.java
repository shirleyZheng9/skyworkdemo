package com.iwhalecloud.bote.service.asr.offline.adpter;

import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.util.GroovyUtil;
import com.iwhalecloud.bote.service.asr.offline.AbstractAsrRecognitionAdapter;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.io.File;
import java.util.Map;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * 自定义语音识别适配器
 * <p>通过自定义脚本实现, 脚本入参要求是File类型，出参是Map类型并包含content字段</p>
 *
 * @author qian.sisheng
 * @since 2025-10-31
 */
@Component
public class CustomAsrRecognitionAdapter extends AbstractAsrRecognitionAdapter {
  @Override
  protected String doRecognize(File audioFile) {
    String code = SystemParameter.CUSTOM_VIDEO_RECOGNITION_SCRIPT.getValueFromDb();
    if (StringUtils.isEmpty(code)) {
      throw new BssException("未配置自定义语音识别脚本");
    }
    Map<String, String> result = GroovyUtil.invoke(code, audioFile);
    return MapUtils.getString(result, "content");
  }

  @Override
  public String getVideoRecognizeType() {
    return "custom";
  }
}
