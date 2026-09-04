package com.iwhalecloud.bote.doc.common.support.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.iwhalecloud.bote.doc.common.utils.ShortTimeFormatter;
import java.io.IOException;
import java.util.Date;

/**
 * 自定义时间格式的特殊序列化类
 *
 * @author Aiqing
 */
public class ShortTimeDateSerializer extends JsonSerializer<Date> {

  @Override
  public void serialize(Date value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
    if (value == null) {
      gen.writeNull();
      return;
    }
    gen.writeString(ShortTimeFormatter.formatDate(value));
  }
}
