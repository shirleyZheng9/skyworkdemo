package com.iwhalecloud.bote.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializerBase;
import com.iwhalecloud.bote.common.interceptor.SessionInterceptor;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * MVC 配置
 *
 * @author chen.linfa
 * @since 2024-07-31
 */
@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

  private final SessionInterceptor sessionInterceptor;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(sessionInterceptor).addPathPatterns("/bote/**");
  }

  /**
   * 定制 MappingJackson2HttpMessageConverter, 将 Long 序列化为 String 以避免数值过大时 JavaScript 中无法精确表示
   * <p>只作用于 HTTP 请求，不影响后端代码中手动使用 ObjectMapper 序列化</p>
   */
  @Override
  public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
    SimpleModule longToStringModule = new SimpleModule("long-to-string");
    longToStringModule.addSerializer(BigDecimal.class, new BigDecimalToStringSerializer());
    longToStringModule.addSerializer(Long.class, ToStringSerializer.instance);
    longToStringModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
    for (HttpMessageConverter<?> converter : converters) {
      if (converter instanceof MappingJackson2HttpMessageConverter) {
        MappingJackson2HttpMessageConverter jsonConverter = (MappingJackson2HttpMessageConverter) converter;
        // 创建新的 ObjectMapper 实例。旧的实例可能已被使用过，再注册新模块不会生效
        ObjectMapper objectMapper = jsonConverter.getObjectMapper().copy();
        objectMapper.registerModule(longToStringModule);
        jsonConverter.setObjectMapper(objectMapper);
        break;
      }
    }
  }

  /**
   * 将 BigDecimal 转为字符串的序列化实现
   */
  private static class BigDecimalToStringSerializer extends ToStringSerializerBase {
    private static final long serialVersionUID = -1L;

    public BigDecimalToStringSerializer() {
      super(BigDecimal.class);
    }

    @Override
    public boolean isEmpty(SerializerProvider prov, Object value) {
      return false;
    }

    @Override
    public final String valueToString(Object value) {
      // 浮点数转字符串，移除多余的小数位 0
      return ((BigDecimal) value).stripTrailingZeros().toPlainString();
    }
  }
}
