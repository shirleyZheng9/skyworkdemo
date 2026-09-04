package com.iwhalecloud.bote.dto.suggestion;

import com.iwhalecloud.bss.litchi.transform.imports.importers.dto.ImportFailedColumn;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

/**
 * 联想术语导入结果
 *
 * @author lizuyin
 * @since 2025-01-29
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "联想术语导入结果")
public class SuggestionTermImportDTO {

  @Schema(description = "成功导入的数据列表")
  private List<Map<String, Object>> successList;

  @Schema(description = "成功导入数量")
  private int successCount;

  @Schema(description = "失败数量")
  private int failCount;

  @Schema(description = "总数量")
  private int totalCount;

  @Schema(description = "失败的列信息")
  private List<ImportFailedColumn> columns;

  @Schema(description = "失败的数据列表")
  private List<Map<String, Object>> failList;

  /**
   * 向 failList 中添加一条失败记录
   *
   * @param rowIndex     定位信息，如 "序号3所在行"
   * @param errorMessage 错误信息，如 "话术类型不存在"
   * @param data         原始数据
   */
  public void failPut(String rowIndex, String errorMessage, Map<String, Object> data) {
    if (failList == null) {
      failList = new java.util.ArrayList<>();
    }
    Map<String, Object> errorEntry = new java.util.HashMap<>();
    errorEntry.put("rowIndex", rowIndex);
    errorEntry.put("reason", errorMessage);
    errorEntry.put("termContent", data.get("termContent"));
    errorEntry.put("termType", data.get("termType"));
    failList.add(errorEntry);
  }
}
