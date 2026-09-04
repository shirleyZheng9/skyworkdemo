package com.iwhalecloud.bote.service.asr.offline;

import java.io.File;

/**
 * 语音识别适配器接口
 *
 * @author qian.sisheng
 * @since 2025-10-27
 */
public interface IAsrRecognitionAdapter {

  /**
   * 语音识别，将音频文件转换为文本
   *
   * @param audioFile 音频文件
   * @return 识别出的文本内容
   */
  String recognize(File audioFile);

  /**
   * 获取语音识别类型
   *
   * @return 适配器名称
   */
  String getVideoRecognizeType();
}
