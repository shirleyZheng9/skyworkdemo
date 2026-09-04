package com.iwhalecloud.bote.dto.datasync;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 数据同步节点分组
 *
 * @author chen.linfa
 * @since 2025-01-14
 */
@Getter
@Setter
@ToString
public class DataSyncGroupDTO {
  @Schema(description = "编码")
  private String code;

  @Schema(description = "名称")
  private String name;

  @Schema(description = "总数")
  private Integer total;

  @Schema(description = "模块排序")
  private Integer groupSortby;

  @Schema(description = "节点列表")
  private List<DataSyncNodeDTO> nodes;
}
