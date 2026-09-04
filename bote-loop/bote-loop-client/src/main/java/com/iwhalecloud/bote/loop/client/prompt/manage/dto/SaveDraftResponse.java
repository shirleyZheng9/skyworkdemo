package com.iwhalecloud.bote.loop.client.prompt.manage.dto;

import com.iwhalecloud.bote.loop.client.base.BaseResp;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.DraftInfoDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 保存草稿响应DTO
 * 对应Thrift: SaveDraftResponse
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaveDraftResponse {

  @Schema(description = "草稿信息")
  private DraftInfoDTO draftInfo;

  @Schema(description = "基础响应信息")
  private BaseResp baseResp;
}
