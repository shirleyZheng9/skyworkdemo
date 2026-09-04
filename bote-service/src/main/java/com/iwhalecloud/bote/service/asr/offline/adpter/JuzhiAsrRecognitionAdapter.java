package com.iwhalecloud.bote.service.asr.offline.adpter;

import com.iwhalecloud.bote.common.util.JuzhiOcrUtil;
import com.iwhalecloud.bote.service.asr.offline.AbstractAsrRecognitionAdapter;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * 聚智语音识别适配器
 *
 * @author qian.sisheng
 * @since 2025-10-27
 */
@Component
public class JuzhiAsrRecognitionAdapter extends AbstractAsrRecognitionAdapter {

  @Override
  protected String doRecognize(File audioFile) {
    return JuzhiOcrUtil.parseVoice(audioFile);
  }

  @Override
  public String getVideoRecognizeType() {
    return "juzhi";
  }

}
