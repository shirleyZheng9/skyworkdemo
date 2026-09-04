package com.iwhalecloud.bote.dto.knowledge.docchain;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * DocChain 主题扩展参数
 */
@Getter
@Setter
@ToString
public class DocChainExtraDTO {
  @Schema(description = "卡片")
  private String cardName;

  @Schema(description = "排序")
  private Integer sortby;

  @Schema(description = "分组定义")
  private List<DocChainExtraGroupDTO> groups;

  /**
   * 分组定义
   */
  @Getter
  @Setter
  @ToString
  @NoArgsConstructor
  @AllArgsConstructor
  public static class DocChainExtraGroupDTO {
    @Schema(description = "分组名称")
    private String groupName;
    @Schema(description = "分组编码")
    private String groupCode;
    @Schema(description = "分组定义")
    private List<DocChainExtraGroupDTO> groups;
    @Schema(description = "属性定义列表")
    private List<DocChainExtraCfgDTO> attrs;
  }
}
