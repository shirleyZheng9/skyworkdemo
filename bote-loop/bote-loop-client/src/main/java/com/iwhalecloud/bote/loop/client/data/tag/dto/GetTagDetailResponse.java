package com.iwhalecloud.bote.loop.client.data.tag.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.base.BaseResp;
import com.iwhalecloud.bote.loop.client.data.domain.tag.TagInfoDTO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Get Tag Detail Response DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetTagDetailResponse {

  @JsonProperty("tags")
  private List<TagInfoDTO> tags;

  @JsonProperty("next_page_token")
  private String nextPageToken;

  @JsonProperty("total")
  private Long total;

  @JsonProperty("BaseResp")
  private BaseResp baseResp;
}
