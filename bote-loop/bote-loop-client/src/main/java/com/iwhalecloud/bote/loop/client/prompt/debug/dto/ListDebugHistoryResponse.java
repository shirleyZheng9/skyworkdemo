package com.iwhalecloud.bote.loop.client.prompt.debug.dto;

import com.iwhalecloud.bote.loop.client.base.BaseResp;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.DebugLogDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 列表调试历史响应DTO
 * 对应Thrift: ListDebugHistoryResponse
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListDebugHistoryResponse {

  @Schema(description = "调试历史列表")
  private List<DebugLogDTO> debugHistory;

  @Schema(description = "是否还有更多")
  private Boolean hasMore;

  @Schema(description = "下一页令牌")
  private String nextPageToken;

  @Schema(description = "基础响应信息")
  private BaseResp baseResp;
}
