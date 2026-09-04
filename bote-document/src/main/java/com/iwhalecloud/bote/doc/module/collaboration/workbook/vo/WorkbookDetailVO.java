package com.iwhalecloud.bote.doc.module.collaboration.workbook.vo;

import com.iwhalecloud.bote.doc.module.collaboration.workbook.dto.ChangesetDTO;
import com.iwhalecloud.bote.doc.module.collaboration.workbook.dto.SheetDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 工作簿详情VO
 *
 * @author Aiqing
 * @since 2025/9/2
 */
@Getter
@Setter
@ToString
@Schema(description = "工作簿详情")
public class WorkbookDetailVO extends UniverResultVO {

  @Schema(description = "工作簿快照信息")
  private WorkbookSnapshot snapshot;

  @Schema(description = "变更集列表")
  private List<ChangesetDTO> changesets;

  /**
   * 工作簿快照
   */
  @Getter
  @Setter
  @ToString
  @Schema(description = "工作簿快照")
  public static class WorkbookSnapshot {

    @Schema(description = "单元ID")
    private String unitID;

    @Schema(description = "类型，默认2:表格")
    private Integer type = 2;

    @Schema(description = "版本号")
    private Long rev;

    @Schema(description = "工作簿信息")
    private Workbook workbook;
  }

  /**
   * 工作簿信息
   */
  @Getter
  @Setter
  @ToString
  @Schema(description = "工作簿信息")
  public static class Workbook {

    @Schema(description = "单元ID")
    private String unitID;

    @Schema(description = "版本号")
    private Long rev;

    @Schema(description = "创建者")
    private String creator;

    @Schema(description = "工作簿名称")
    private String name;

    @Schema(description = "工作表顺序")
    private List<String> sheetOrder;

    @Schema(description = "工作表信息")
    private Map<String, SheetDTO> sheets;

    @Schema(description = "资源列表")
    private List<Object> resources;

    @Schema(description = "块元数据")
    private Map<String, BlockMeta> blockMeta;

    @Schema(description = "原始元数据")
    private String originalMeta;
  }

  /**
   * 块元数据
   */
  @Getter
  @Setter
  @ToString
  @Schema(description = "块元数据")
  public static class BlockMeta {

    @Schema(description = "工作表ID")
    private String sheetID;

    @Schema(description = "块列表")
    private List<Long> blocks;
  }
}
