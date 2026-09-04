package com.iwhalecloud.bote.dto.asr;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 音频解析模型句子内容
 *
 * @author qian.sisheng
 * @since 2025-10-27
 */

@Getter
@Setter
@ToString
public class SentenceDTO {
  /** 句子内容 */
  private String text;
  /** 句子开始时间 */
  private Long start;
  /** 句子结束时间 */
  private Long end;
  /** 句子时间戳 */
  private List<List<Long>> timestamp;
  /** 句子原始内容,不带符号 */
  @JsonProperty("raw_text")
  private String rawText;
  /** 句子说话人 */
  private Integer spk;
  /** 本句子对应的截图文件id */
  private Long fileId;
}
