package com.iwhalecloud.bote.common.converter;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * 字符串转为本地日期
 *
 * @author bianjp
 * @since 2024-11-18
 */
@Component("boteStringToLocalDateConverter")
@RequiredArgsConstructor
public class StringToLocalDateConverter implements Converter<String, LocalDate> {
  private final StringToDateConverter stringToDateConverter;

  @Nullable
  @Override
  public LocalDate convert(String source) {
    // 复用日期转换器
    Date date = stringToDateConverter.convert(source);
    if (date != null) {
      return date.toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDate();
    }
    return null;
  }
}
