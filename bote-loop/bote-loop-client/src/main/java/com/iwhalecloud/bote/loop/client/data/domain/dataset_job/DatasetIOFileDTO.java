package com.iwhalecloud.bote.loop.client.data.domain.dataset_job;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.StorageProviderDTO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据集IO文件数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DatasetIOFileDTO {

  @JsonProperty("provider")
  private StorageProviderDTO provider;

  @JsonProperty("path")
  private String path;

  @JsonProperty("format")
  private FileFormatDTO format;

  @JsonProperty("compress_format")
  private FileFormatDTO compressFormat;

  @JsonProperty("files")
  private List<String> files;
}
