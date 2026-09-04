package com.iwhalecloud.bote.common.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * 字节数组转为资源
 *
 * <p>由于 Spring 查找 converter 的优先级问题，它会先匹配到 {@link org.springframework.core.convert.support.ArrayToObjectConverter} 而非 {@link ObjectToResourceConverter},
 * 因此需要单独注册这个转换器。</p>
 *
 * @author qian.sisheng
 * @see org.springframework.core.convert.support.GenericConversionService.Converters#find
 * @since 2025-07-08
 */
@Component("boteByteArrayToResourceConverter")
public class ByteArrayToResourceConverter implements Converter<byte[], Resource> {

  @Override
  public Resource convert(byte[] source) {
    return new ByteArrayResource(source) {
      @Override
      public String getFilename() {
        return "默认名称";
      }
    };
  }

}
