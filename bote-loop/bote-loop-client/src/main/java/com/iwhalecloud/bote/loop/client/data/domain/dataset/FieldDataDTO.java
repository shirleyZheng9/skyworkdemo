package com.iwhalecloud.bote.loop.client.data.domain.dataset;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 字段数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FieldDataDTO {

  @JsonProperty("key")
  private String key;

  @JsonProperty("name")
  private String name;

  @JsonProperty("content_type")
  private ContentTypeDTO contentType;

  @JsonProperty("content")
  private String content;

  @JsonProperty("attachments")
  private List<ObjectStorageDTO> attachments;

  @JsonProperty("format")
  private FieldDisplayFormatDTO format;

  @JsonProperty("parts")
  private List<FieldDataDTO> parts;
}
