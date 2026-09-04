package com.iwhalecloud.bote.loop.client.data.domain.tag;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Base Info DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BaseInfoDTO {

  @JsonProperty("created_by")
  private String createdBy;

  @JsonProperty("created_at")
  private Long createdAt;

  @JsonProperty("updated_by")
  private String updatedBy;

  @JsonProperty("updated_at")
  private Long updatedAt;
}
