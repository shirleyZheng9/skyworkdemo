package com.iwhalecloud.bote.loop.client.data.domain.tag;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 连续数字规格数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContinuousNumberSpecDTO {

  @JsonProperty("min_value")
  private Double minValue;

  @JsonProperty("min_value_description")
  private String minValueDescription;

  @JsonProperty("max_value")
  private Double maxValue;

  @JsonProperty("max_value_description")
  private String maxValueDescription;
}
