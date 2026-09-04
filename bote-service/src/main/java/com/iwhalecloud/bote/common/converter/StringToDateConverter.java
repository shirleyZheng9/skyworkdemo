package com.iwhalecloud.bote.common.converter;

import java.text.ParseException;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * 字符串转为日期
 *
 * <p>用于兼容多种日期格式</p>
 *
 * @author bianjp
 * @since 2019-06-28
 */
@Component("boteStringToDateConverter")
public class StringToDateConverter implements Converter<String, Date> {
  /** 支持的日期格式 */
  private static final String[] patterns = new String[]{
    "yyyy-MM-dd HH:mm:ss",
    "yyyy-MM-dd HH:mm:ss.SSS",
    "yyyy-MM-dd",
    "yyyy年MM月dd日",
    "dd/MM/yyyy",
    "dd/MM/yyyy HH:mm:ss",
    "yyyy/MM/dd",
    "yyyy/MM/dd HH:mm:ss",
    "yyyy-MM-dd HH:mm:ssZ",
    "yyyy-MM-dd HH:mm:ss.SSSZ",
    "yyyy-MM-dd HH:mm:ssZZ",
    "yyyy-MM-dd'T'HH:mm:ss",
    "yyyy-MM-dd'T'HH:mm:ssZ",
    "yyyy-MM-dd'T'HH:mm:ssZZ"
  };

  @Nullable
  @Override
  public Date convert(String source) {
    if (StringUtils.isEmpty(source)) {
      return null;
    }

    // 如果是数字，当做时间戳
    if (StringUtils.isNumeric(source)) {
      return new Date(Long.parseLong(source, 10));
    }

    try {
      return DateUtils.parseDateStrictly(source, patterns);
    }
    catch (ParseException e) {
      throw new IllegalArgumentException("日期格式非法: " + source, e);
    }
  }

}
