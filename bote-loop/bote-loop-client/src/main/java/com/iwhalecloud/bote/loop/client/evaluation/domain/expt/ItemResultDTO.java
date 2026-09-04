package com.iwhalecloud.bote.loop.client.evaluation.domain.expt;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 项目结果数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "项目结果数据传输对象")
public class ItemResultDTO {

  @Schema(description = "项目ID")
  private Long itemId;

  @Schema(description = "轮次结果列表")
  private List<TurnResultDTO> turnResults;

  @Schema(description = "系统信息")
  private ItemSystemInfoDTO systemInfo;

  @Schema(description = "项目索引")
  private Long itemIndex;
}
