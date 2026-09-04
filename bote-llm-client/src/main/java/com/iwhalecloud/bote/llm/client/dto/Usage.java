package com.iwhalecloud.bote.llm.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 计量信息
 *
 * @author bianjp
 * @since 2024-08-01
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
@JsonNaming(SnakeCaseStrategy.class)
public class Usage {
  /** 提示词 token 数量 */
  private Integer promptTokens;
  /** 回复 token 数量 */
  private Integer completionTokens;
  /** token 总数(提示词 + 回复) */
  private Integer totalTokens;
  /** 提示词 token 详情 */
  private Object promptTokensDetails;
  /** 回复 token 详情 */
  private Object completionTokensDetails;
}
