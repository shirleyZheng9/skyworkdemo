package com.iwhalecloud.bote.loop.client.data.domain.tag;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 标签内容规格数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TagContentSpecDTO {

  @JsonProperty("continuous_number_spec")
  private ContinuousNumberSpecDTO continuousNumberSpec;
}
