package com.iwhalecloud.bote.common.converter;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.converters.ReadConverterContext;
import com.alibaba.excel.converters.WriteConverterContext;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.data.WriteCellData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

/**
 * easyExcel长整型ID文本转换器
 *
 * @author qian.sisheng
 * @since 2025-11-12
 */
@SuppressWarnings("PMD.GuardLogStatement")
public class LongTextIdConverter implements Converter<Long> {
  /** 日志对象 */
  private final Logger logger = LoggerFactory.getLogger(LongTextIdConverter.class);

  /**
   * 支持的Java类型
   */
  @Override
  public Class<?> supportJavaTypeKey() {
    return Long.class;
  }

  /**
   * 支持的Excel单元格类型
   *
   * @return Excel单元格类型：字符串
   */
  @Override
  public CellDataTypeEnum supportExcelTypeKey() {
    return CellDataTypeEnum.STRING;
  }

  /**
   * 读取时转换为Java数据
   *
   * @param context 读取转换上下文
   * @return Long值，读取失败返回null
   */
  @Override
  @Nullable
  public Long convertToJavaData(ReadConverterContext<?> context) {
    try {
      String s = context.getReadCellData().getStringValue();
      if (s == null || s.trim().isEmpty()) {
        return null;
      }
      return Long.valueOf(s.trim());
    } catch (Exception e) {
      logger.error("Convert to java Long failed: message = {}", e.getMessage(), e);
      return null;
    }
  }

  /**
   * 写入时转换为Excel数据（文本）
   *
   * @param context 写入转换上下文，包含Long值
   * @return 文本类型的单元格数据
   */
  @Override
  public WriteCellData<?> convertToExcelData(WriteConverterContext<Long> context) {
    Long value = context.getValue();
    if (value == null) {
      return new WriteCellData<>("");
    }
    return new WriteCellData<>(String.valueOf(value));
  }
}
