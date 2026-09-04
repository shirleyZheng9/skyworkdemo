package com.iwhalecloud.bote.loop.client.data.domain.dataset;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 排序数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderByDTO {

  @JsonProperty("field")
  private String field;

  @JsonProperty("is_asc")
  private Boolean isAsc;
}
