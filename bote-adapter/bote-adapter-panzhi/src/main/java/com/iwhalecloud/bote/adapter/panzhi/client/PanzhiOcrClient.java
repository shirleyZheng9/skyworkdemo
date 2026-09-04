package com.iwhalecloud.bote.adapter.panzhi.client;

import com.iwhalecloud.bote.adapter.panzhi.helper.PanzhiSpeechRecognitionHelper;
import com.iwhalecloud.bote.service.external.PanzhiClient;
import java.io.File;
import org.springframework.stereotype.Service;

/**
 * 磐智 OCR 服务
 *
 * @author qian.sisheng
 * @since 2025-09-13
 */
@Service
public class PanzhiOcrClient implements PanzhiClient {

  @Override
  public String parseVoice(File file) {
    return PanzhiSpeechRecognitionHelper.recognizeSpeech(file);
  }
}
