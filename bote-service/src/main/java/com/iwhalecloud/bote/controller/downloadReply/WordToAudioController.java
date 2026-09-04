package com.iwhalecloud.bote.controller.downloadReply;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.dto.reply.TtsRequest;
import com.iwhalecloud.bote.service.reply.IWordToAudioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "对话：下载音频")
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX)
@RequiredArgsConstructor
public class WordToAudioController {

  private final IWordToAudioService wordToAudioService;

  /**
   * 清空音色 ID 缓存
   *
   * <p>仅供特殊场景下使用(比如缓存的 voiceId 在 TTS 服务器上存在，但内容不是我们上传的）</p>
   */
  @Operation(hidden = true)
  @PostMapping("wordToAudio/clearCache")
  public void clearCache() {
    wordToAudioService.clearCache();
  }

  /**
   * 文字转语音
   */
  @PostMapping("/download/wordToAudio")
  public void wordToAudio(@RequestBody TtsRequest request, HttpServletResponse response) {
    wordToAudioService.tts(request, response);
  }

}
