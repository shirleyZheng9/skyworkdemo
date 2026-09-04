package com.iwhalecloud.bote.service.asr.offline.adpter;

import com.iwhalecloud.bote.service.asr.offline.AbstractAsrRecognitionAdapter;
import com.iwhalecloud.bote.service.external.PanzhiClient;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.File;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 盘智语音识别适配器
 *
 * @author qian.sisheng
 * @since 2025-10-27
 */
@Component
public class PanzhiAsrRecognitionAdapter extends AbstractAsrRecognitionAdapter {
  private final PanzhiClient panzhiClient;

  @SuppressFBWarnings("CT_CONSTRUCTOR_THROW")
  public PanzhiAsrRecognitionAdapter(ObjectProvider<PanzhiClient> panzhiClient) {
    this.panzhiClient = panzhiClient.getIfAvailable();
  }

  @Override
  protected String doRecognize(File audioFile) {
    Assert.notNull(panzhiClient, "未找到磐智ocr客户端实例：panzhiClient，请检查配置是否正确。");
    return panzhiClient.parseVoice(audioFile);
  }

  @Override
  public String getVideoRecognizeType() {
    return "panzhi";
  }

}
