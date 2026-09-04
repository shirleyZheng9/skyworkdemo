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
 * Batch Get Tags Response DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchGetTagsResponse {

  @JsonProperty("tag_info_list")
  private List<TagInfoDTO> tagInfoList;

  @JsonProperty("BaseResp")
  private BaseResp baseResp;
}
