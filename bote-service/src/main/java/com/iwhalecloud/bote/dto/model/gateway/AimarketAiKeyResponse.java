package com.iwhalecloud.bote.dto.model.gateway;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 天工企业 AI Key 查询响应
 *
 * @author jiangm
 * @since 2026-08-06
 */
@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class AimarketAiKeyResponse {
  private Integer code;
  private String msg;
  private List<String> data;
}
