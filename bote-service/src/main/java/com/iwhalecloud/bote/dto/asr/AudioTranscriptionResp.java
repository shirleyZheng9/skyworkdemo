package com.iwhalecloud.bote.dto.asr;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 音频转写结果
 *
 * @author qian.sisheng
 * @since 2025-10-27
 */
@Getter
@Setter
@ToString
public class AudioTranscriptionResp {
  /** 状态,success:成功 */
  @JsonProperty("status_text")
  private String statusText;
  /** 句子内容 */
  private List<SentenceDTO> sentences;
}
