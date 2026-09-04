package com.iwhalecloud.bote.service.asr.offline;

import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import org.springframework.util.Assert;

/**
 * 语音识别适配器抽象类
 *
 * @author qian.sisheng
 * @since 2025-10-27
 */
@SuppressWarnings("PMD.GuardLogStatement")
public abstract class AbstractAsrRecognitionAdapter implements IAsrRecognitionAdapter {

  protected final Logger logger = LoggerFactory.getLogger(getClass());

  @Override
  public String recognize(File audioFile) {
    if (audioFile == null || !audioFile.exists()) {
      throw new BssException("音频文件不存在");
    }
    try {
      return doRecognize(audioFile);
    }
    catch (Exception e) {
      logger.error("Speech recognition failed, file: {}", audioFile.getAbsolutePath(), e);
      throw BaseErrorConstant.OCR_IDENTIFY_WORDS_ERROR.toException(e, "语音识别失败：" + e.getMessage());
    }
  }

  /**
   * 获取语音识别的 URL,语音识别URL统一使用 VIDEO_OCR_API_URL
   */
  protected String getRequiredVideoRecognizeUrl() {
    String url = SystemParameter.VIDEO_OCR_API_URL.getValueFromDb();
    Assert.hasText(url, "语音识别 API 地址不能为空");
    return url;
  }

  /**
   * 执行具体的语音识别逻辑
   *
   * @param audioFile 音频文件
   * @return 识别出的文本内容
   */
  protected abstract String doRecognize(File audioFile);
}
