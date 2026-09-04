package com.iwhalecloud.bote.loop.client.data.domain.dataset;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 项目错误详情数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemErrorDetailDTO {

  @JsonProperty("message")
  private String message;

  @JsonProperty("index")
  private Integer index;

  @JsonProperty("start_index")
  private Integer startIndex;

  @JsonProperty("end_index")
  private Integer endIndex;
}
