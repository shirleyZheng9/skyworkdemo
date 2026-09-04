package com.iwhalecloud.bote.dto.knowledge.docchain.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 生成 API Key 响应
 *
 * @author chen.linfa
 * @since 2025-10-20
 */
@Getter
@Setter
@ToString
public class GenerateApiKeyResponse {
  /** 是否成功 */
  private Boolean success;
  /** 异常信息 */
  private String err;
  /** 信息 */
  private GenerateApiKey data;

  @Getter
  @Setter
  @ToString
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonNaming(SnakeCaseStrategy.class)
  public static class GenerateApiKey {
    private String apiKey;
  }
}
