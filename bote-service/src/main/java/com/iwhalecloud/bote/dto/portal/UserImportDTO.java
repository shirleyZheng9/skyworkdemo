package com.iwhalecloud.bote.dto.portal;

import com.iwhalecloud.bss.litchi.transform.imports.importers.dto.ImportFailedColumn;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 用户导入结果
 *
 * @author lizuyin
 * @since 2025/6/3
 */
@Getter
@Setter
@ToString(callSuper = true)
public class UserImportDTO {

  private List<Map<String, Object>> successList;

  private int successCount;

  private int failCount;

  private int totalCount;

  private List<ImportFailedColumn> columns;

  private List<Map<String, Object>> failList;

  /**
   * 向 failList 中添加一条失败记录
   *
   * @param rowIndex     定位信息，如 "2行3列"
   * @param errorMessage 错误信息，如 "用户名为空"
   */
  public void failPut(String rowIndex, String errorMessage, Map<String, Object> data) {
    if (failList == null) {
      failList = new ArrayList<>();
    }
    Map<String, Object> errorEntry = new java.util.HashMap<>();
    errorEntry.put("rowIndex", rowIndex);
    errorEntry.put("reason", errorMessage);
    errorEntry.put("realName", data.get("realName"));
    errorEntry.put("userName", data.get("userName"));
    errorEntry.put("phoneNo", data.get("phoneNo"));
    errorEntry.put("email", data.get("email"));
    errorEntry.put("remark", data.get("remark"));
    failList.add(errorEntry);
  }

}
