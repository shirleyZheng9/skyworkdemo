package com.iwhalecloud.bote.service.reply;

import com.iwhalecloud.bote.dto.reply.TtsRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 文字转语音服务
 *
 * @author qian.sisheng
 * @since 2025-11-21
 */
public interface IWordToAudioService {

  /**
   * 文字转语音，结果上传到文件服务器
   *
   * @param request 音频参数
   * @return 文件 ID
   */
  Long ttsAndUpload(TtsRequest request);

  /**
   * 文字转语音，结果返回给前端
   *
   * @param request 请求参数
   * @param response 响应
   */
  void tts(TtsRequest request, HttpServletResponse response);

  /**
   * 清空音色 ID 缓存
   */
  void clearCache();
}
