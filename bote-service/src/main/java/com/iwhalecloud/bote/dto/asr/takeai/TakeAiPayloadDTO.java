package com.iwhalecloud.bote.dto.asr.takeai;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * TakeAi 协议消息载荷 DTO
 *
 * @author qian.sisheng
 * @since 2026-01-09
 */
@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TakeAiPayloadDTO {
  /** 音频格式 */
  private String format;
  /** 采样率 */
  @JsonProperty("sample_rate")
  private Integer sampleRate;
  /** 识别延迟 */
  private Integer delay;
  /** 热词 */
  @JsonProperty("hot_words")
  private String hotWords;
  /** 强制句尾最大时长, 默认30000ms */
  @JsonProperty("max_segment_duration_ms")
  private Integer maxSegmentDurationMs;
  /** 是否开启纠错 */
  @JsonProperty("enable_correction")
  private Boolean enableCorrection;
  /** 是否开启语义断句 */
  @JsonProperty("enable_semantic_sentence_detection")
  private Boolean enableSemanticSentenceDetection;
  /** 是否开启语气词过滤 */
  @JsonProperty("disfluency")
  private Boolean disfluency;
  /** 是否开启词级时间戳 */
  @JsonProperty("enable_words")
  private Boolean enableWords;
  /** 是否为最终结果 */
  @JsonProperty("is_final")
  private Boolean finaled;
  /** 识别结果列表 最后会返字符串 "最终识别结果", 识别结果为TakeAiResultDTO */
  private Object result;
}
