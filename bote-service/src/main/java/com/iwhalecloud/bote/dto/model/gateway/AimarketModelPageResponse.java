package com.iwhalecloud.bote.dto.model.gateway;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 天工 aimarket 模型分页响应
 *
 * @author jiangm
 * @since 2026-08-06
 */
@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class AimarketModelPageResponse {
  private Integer code;
  private String msg;
  private PageData data;

  @Getter
  @Setter
  @ToString
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class PageData {
    private List<AimarketModelRecord> records;
    private Long total;
    private Long size;
    private Long current;
    private Long pages;
  }
}
