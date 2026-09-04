package com.iwhalecloud.bote.llm;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bote.llm.client.config.LlmProperties;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.Getter;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.Assert;
import org.yaml.snakeyaml.Yaml;

/**
 * 模型配置加载器
 *
 * @author bianjp
 * @since 2024-10-22
 */
@SuppressWarnings("PMD.MissingStaticMethodInNonInstantiatableClass")
public final class ModelConfigLoader {
  /** 大模型配置映射，key 为名称（缩写，不是调用大模型用的模型名称） */
  @Getter
  private static final Map<String, LlmProperties> models = loadModels();

  private ModelConfigLoader() {
  }

  /**
   * 加载配置文件中的大模型列表
   */
  private static Map<String, LlmProperties> loadModels() {
    try (InputStream inputStream = new ClassPathResource("model-config.yml").getInputStream()) {
      Map<String, Object> config = new Yaml().load(inputStream);
      // 默认接口地址
      String defaultApiUrl = MapUtils.getString(config, "defaultApiUrl");
      Assert.hasLength(defaultApiUrl, "defaultApiUrl 不能为空");
      // 默认密钥
      String defaultApiKey = loadDefaultApiKey();
      Assert.hasLength(defaultApiKey, "defaultApiKey 不能为空");
      Map<String, LlmProperties> map = JsonUtil.convert(config.get("models"), new TypeReference<Map<String, LlmProperties>>() {
      });
      Assert.notEmpty(map, "models 不能为空");

      for (LlmProperties properties : map.values()) {
        if (StringUtils.isEmpty(properties.getUrl())) {
          properties.setUrl(defaultApiUrl);
        }
        if (StringUtils.isEmpty(properties.getApiKey())) {
          properties.setApiKey(defaultApiKey);
        }
      }

      return map;
    }
    catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * 加载默认 API 密钥
   */
  private static String loadDefaultApiKey() throws IOException {
    // 通过环境变量配置密钥以避免泄露
    String defaultApiKey = System.getenv("LLM_TEST_DEFAULT_API_KEY");
    if (StringUtils.isEmpty(defaultApiKey)) {
      // 支持通过本地文件配置以方便本地调试
      ClassPathResource apiKeyResource = new ClassPathResource("model-api-key.txt");
      if (apiKeyResource.exists()) {
        try (InputStream apiKeyInputStream = apiKeyResource.getInputStream()) {
          defaultApiKey = StringUtils.trim(IOUtils.toString(apiKeyInputStream, StandardCharsets.UTF_8));
        }
      }
    }
    return defaultApiKey;
  }
}
