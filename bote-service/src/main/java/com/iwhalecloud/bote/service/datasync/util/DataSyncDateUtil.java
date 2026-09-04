package com.iwhalecloud.bote.service.datasync.util;

import com.iwhalecloud.bss.litchi.util.DateUtil;
import com.iwhalecloud.bote.dto.datasync.DataSyncTableDefinition;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;

/**
 * 数据同步辅助工具类 - 时间字段
 *
 * @author chen.linfa
 * @since 2024-10-22
 */
public final class DataSyncDateUtil {
  private DataSyncDateUtil() {
  }

  /**
   * 日期格式转为字符串格式
   */
  public static void toString(DataSyncTableDefinition definition) {
    if (CollectionUtils.isEmpty(definition.getDateColumns())) {
      return;
    }
    for (Map<String, Object> record : definition.getDataRecords()) {
      // 只遍历日期字段，减少遍历次数
      for (String dateColumn : definition.getDateColumns()) {
        Object date = record.get(dateColumn);
        if (date != null) {
          record.put(dateColumn, DateUtil.format((Date) date));
        }
      }
    }
  }

  /**
   * 字符格式转为日期格式
   */
  public static void toDate(List<DataSyncTableDefinition> definitions) {
    for (DataSyncTableDefinition definition : definitions) {
      if (CollectionUtils.isEmpty(definition.getDateColumns())) {
        continue;
      }
      for (Map<String, Object> record : definition.getDataRecords()) {
        // 只遍历日期字段，减少遍历次数
        for (String dateColumn : definition.getDateColumns()) {
          Object date = record.get(dateColumn);
          if (date != null) {
            record.put(dateColumn, DateUtil.parse((String) date));
          }
        }
      }
    }
  }
}
