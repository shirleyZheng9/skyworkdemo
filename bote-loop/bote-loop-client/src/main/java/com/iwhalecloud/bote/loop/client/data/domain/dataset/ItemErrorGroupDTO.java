package com.iwhalecloud.bote.loop.client.data.domain.dataset;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 项目错误组数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemErrorGroupDTO {

  @JsonProperty("type")
  private ItemErrorTypeDTO type;

  @JsonProperty("summary")
  private String summary;

  @JsonProperty("error_count")
  private Integer errorCount;

  @JsonProperty("details")
  private List<ItemErrorDetailDTO> details;
}
