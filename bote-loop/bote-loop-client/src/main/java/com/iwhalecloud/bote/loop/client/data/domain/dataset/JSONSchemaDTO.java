package com.iwhalecloud.bote.loop.client.data.domain.dataset;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JSON Schema DTO
 * 对应Go: JSONSchema
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JSONSchemaDTO {

  @JsonProperty("raw")
  private String raw;
}
