package com.iwhalecloud.bote.loop.client.data.domain.dataset;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 对象存储数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ObjectStorageDTO {

  @JsonProperty("provider")
  private StorageProviderDTO provider;

  @JsonProperty("name")
  private String name;

  @JsonProperty("uri")
  private String uri;

  @JsonProperty("url")
  private String url;

  @JsonProperty("thumb_url")
  private String thumbUrl;
}
