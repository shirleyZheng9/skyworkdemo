package com.iwhalecloud.bote.loop.client.data.domain.dataset;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 项目数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemDataDTO {

  @JsonProperty("id")
  private Long id;

  @JsonProperty("data")
  private List<FieldDataDTO> data;
}
