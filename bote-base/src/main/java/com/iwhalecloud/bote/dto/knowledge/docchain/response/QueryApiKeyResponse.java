package com.iwhalecloud.bote.dto.knowledge.docchain.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 查询 API Key 响应
 *
 * @author chen.linfa
 * @since 2025-10-20
 */
@Getter
@Setter
@ToString
public class QueryApiKeyResponse {
  /** 是否成功 */
  private Boolean success;
  /** 异常信息 */
  private String err;
  /** 信息 */
  private List<ApiKey> data;

  @Getter
  @Setter
  @ToString
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonNaming(SnakeCaseStrategy.class)
  public static class ApiKey {
    private String apiKey;
    private String state;
  }
}
