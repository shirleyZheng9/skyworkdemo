package com.iwhalecloud.bote.loop.client.data.domain.dataset;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 多模态规格数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MultiModalSpecDTO {

  @JsonProperty("max_file_count")
  private Long maxFileCount;

  @JsonProperty("max_file_size")
  private Long maxFileSize;

  @JsonProperty("supported_formats")
  private List<String> supportedFormats;
}
