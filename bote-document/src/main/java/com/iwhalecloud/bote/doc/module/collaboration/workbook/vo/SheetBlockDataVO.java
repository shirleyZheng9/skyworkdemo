package com.iwhalecloud.bote.doc.module.collaboration.workbook.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * sheet的数据对象VO
 *
 * @author Aiqing
 * @since 2025/9/2
 */
@Getter
@Setter
@ToString
public class SheetBlockDataVO extends UniverResultVO {

  @Schema(description = "block数据")
  private BlockData block;

  /**
   * 默认空数据
   *
   * @return vo对象
   */
  public static SheetBlockDataVO emptyData() {
    SheetBlockDataVO blockDataVO = new SheetBlockDataVO();
    blockDataVO.setBlock(new BlockData());
    return blockDataVO;
  }

  @Getter
  @Setter
  @ToString
  @JsonInclude(Include.NON_NULL)
  public static final class BlockData {
    private Long id;
    private Integer endRow;
    private RowData data;
  }

  @Getter
  @Setter
  @ToString
  public static final class RowData {
    @Schema(description = "单元格的值， { rowNum : { cellNum : 单元格数据 } }")
    private Map<Integer, Map<Integer, CellData>> data;
  }

  @Getter
  @Setter
  @ToString
  private static final class CellData {
    @Schema(description = "单元格值")
    private Object v;
    @Schema(description = "单元格类型")
    private Integer t;
  }

}
