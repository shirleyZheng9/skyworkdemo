package com.iwhalecloud.bote.loop.client.data.tag.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.base.BaseResp;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Batch Update Tag Status Response DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchUpdateTagStatusResponse {

  @JsonProperty("err_info")
  private Map<Long, String> errInfo;

  @JsonProperty("BaseResp")
  private BaseResp baseResp;
}
