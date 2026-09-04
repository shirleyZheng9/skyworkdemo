package com.iwhalecloud.bote.dto.intent;

import com.iwhalecloud.bss.litchi.transform.imports.importers.dto.ImportFailedColumn;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 意图问句导入结果
 *
 * @author lizuyin
 * @since 2026-02-26
 */
@Getter
@Setter
@ToString
public class IntentQuestionImportDTO {

  /** 成功导入的数据列表 */
  private List<Map<String, Object>> successList;
  /** 成功导入的记录数量 */
  private int successCount;
  /** 导入失败的记录数量 */
  private int failCount;
  /** 总记录数量 */
  private int totalCount;
  /** 导入失败的列信息（用于Excel模板生成） */
  private List<ImportFailedColumn> columns;
  /** 导入失败的详细记录列表 */
  private List<Map<String, Object>> failList;

  /**
   * 向失败列表中添加一条失败记录
   *
   * @param rowIndex 行号定位信息，如 "第2行"
   * @param errorMessage 错误原因描述
   * @param data 原始数据，包含用户输入的所有字段信息
   */
  public void failPut(String rowIndex, String errorMessage, Map<String, Object> data) {
    if (failList == null) {
      failList = new ArrayList<>();
    }
    Map<String, Object> errorEntry = new java.util.HashMap<>();
    errorEntry.put("rowIndex", rowIndex);
    errorEntry.put("reason", errorMessage);
    errorEntry.put("question", data.get("question"));
    errorEntry.put("attribute", data.get("attribute"));
    failList.add(errorEntry);
  }
}


