package com.iwhalecloud.bote.service.asr.offline.adpter;

import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.dto.asr.AudioTranscriptionResp;
import com.iwhalecloud.bote.dto.asr.SentenceDTO;
import com.iwhalecloud.bote.service.asr.offline.AbstractAsrRecognitionAdapter;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.io.File;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * takeAi 语音识别适配器
 *
 * @author qian.sisheng
 * @since 2025-10-27
 */
@Component
public class TakeAiAsrRecognitionAdapter extends AbstractAsrRecognitionAdapter {

  @Override
  protected String doRecognize(File audioFile) {
    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("file", new FileSystemResource(audioFile));
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    AudioTranscriptionResp response = HttpUtil.post(getRequiredVideoRecognizeUrl(), body, new ParameterizedTypeReference<>() {
    }, headers);
    if (response == null) {
      throw new BssException("语音识别失败，响应为空");
    }
    String statusText = response.getStatusText();
    if (!"success".equals(statusText)) {
      throw new BssException("语音识别失败，状态文本: " + statusText);
    }
    List<SentenceDTO> sentences = response.getSentences();
    // 拼接识别结果
    StringBuilder content = new StringBuilder();
    for (SentenceDTO sentence : CollectionUtils.emptyIfNull(sentences)) {
      content.append(sentence.getText());
    }
    return content.toString();
  }

  @Override
  public String getVideoRecognizeType() {
    return "takeAi";
  }
}
