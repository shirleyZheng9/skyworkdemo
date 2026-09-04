/*
 * Copyright (c) 2011-2024, baomidou (jobob@qq.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.iwhalecloud.bote.doc.common.mybatis.parser.cache;

import com.google.common.cache.Cache;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * jsqlparser 缓存 安全序列化 Guava Cache 缓存实现
 * 使用自定义的序列化实现替代不安全的Spring SerializationUtils
 *
 * @author miemie
 * @since 2023-08-05
 */
@SuppressWarnings("PMD.GuardLogStatement")
public class JdkSerialCaffeineJsqlParseCache extends AbstractCaffeineJsqlParseCache {

  private static final Logger logger = LoggerFactory.getLogger(JdkSerialCaffeineJsqlParseCache.class);

  public JdkSerialCaffeineJsqlParseCache(Cache<String, byte[]> cache) {
    super(cache);
  }

  @Override
  public byte[] serialize(Object obj) {
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(baos)) {
      oos.writeObject(obj);
      return baos.toByteArray();
    }
    catch (IOException e) {
      logger.error("序列化对象失败: {}", obj.getClass().getSimpleName(), e);
      throw new RuntimeException("序列化失败", e);
    }
  }

  @Override
  public Object deserialize(String sql, byte[] bytes) {
    try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
         ObjectInputStream ois = new ObjectInputStream(bais)) {
      return ois.readObject();
    }
    catch (IOException | ClassNotFoundException e) {
      logger.error("反序列化对象失败: sql={}", sql, e);
      throw new RuntimeException("反序列化失败", e);
    }
  }
}
