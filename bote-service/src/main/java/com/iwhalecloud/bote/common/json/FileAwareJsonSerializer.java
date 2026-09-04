package com.iwhalecloud.bote.common.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializerBase;
import com.iwhalecloud.bss.litchi.util.DateUtil;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * 特殊处理文件类型的 JSON 序列化器
 *
 * <p>编排服务及其步骤的入参、出参中可能包含文件类型，大多实现类默认不支持 JSON 序列化，会导致返回给前端、存储到数据库/缓存时报错，此处将文件类型序列化为字符串以避免报错。</p>
 *
 * <p>将文件序列化为字符串时只包含文件的简要描述，不需要包含文件内容（文件可能是二进制的，或者体积很大，不适合包含）。</p>
 *
 * @author bianjp
 * @since 2024-08-29
 */
public class FileAwareJsonSerializer extends StdSerializer<Object> {
  private static final long serialVersionUID = 1L;

  /** 转为字符串序列化器 */
  private static final JsonSerializer<Object> toStringSerializer = new ToStringSerializerBase(Object.class) {
    private static final long serialVersionUID = 1L;

    @Override
    public String valueToString(Object value) {
      if (value instanceof MultipartFile) {
        return value.getClass().getSimpleName() + " [" + ((MultipartFile) value).getName() + "]";
      }
      if (value instanceof LocalDateTime) {
        return DateUtil.format((LocalDateTime) value);
      }
      return value.toString();
    }
  };
  // @formatter:off
  /** ObjectMapper 实例 */
  private static final ObjectMapper objectMapper = JsonUtil.getObjectMapper().copy()
    .registerModule(new SimpleModule("file-to-string")
      .addSerializer(MultipartFile.class, toStringSerializer)
      .addSerializer(Resource.class, toStringSerializer)
      .addSerializer(InputStream.class, toStringSerializer)
      .addSerializer(OutputStream.class, toStringSerializer)
      .addSerializer(LocalDateTime.class, toStringSerializer)
      );
  // @formatter:on

  public FileAwareJsonSerializer() {
    super(Object.class);
  }

  @Override
  public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
    // value 一般是嵌套的 Map, 没什么好方法可以指定 Jackson 对所有嵌套属性中的文件类型使用特殊的序列化器，只好使用特定的 ObjectMapper 转换一下
    Object result = objectMapper.convertValue(value, Object.class);
    gen.writeObject(result);
  }
}
