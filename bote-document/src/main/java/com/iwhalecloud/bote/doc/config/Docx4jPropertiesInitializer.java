package com.iwhalecloud.bote.doc.config;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.docx4j.Docx4jProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * 在应用启动时加载 docx4j 配置并注入到 Docx4jProperties。
 * <p>
 * 优先级（高到低）：
 * 1）Spring Environment 中已有的同名配置（如环境变量、-D、application 中的 docx4j.openpackaging.*）；
 * 2）配置文件中的值（仅对 Environment 中不存在的 key 做补充）。配置文件来源顺序：
 *    a）{@value #CONFIG_FILE_PROPERTY} 指定的文件路径；
 *    b）{@code ZSMART_HOME/etc/docx4j.properties}；
 *    c）{@code ZSMART_HOME/etc/coreConfig.properties}（直接从此文件读取 docx4j.* 键）；
 *    d）classpath {@value #DEFAULT_CLASSPATH_PATH}。
 * </p>
 */
@Component
@SuppressWarnings("PMD.GuardLogStatement")
public class Docx4jPropertiesInitializer implements ApplicationListener<ApplicationReadyEvent> {

  private static final Logger logger = LoggerFactory.getLogger(Docx4jPropertiesInitializer.class);

  /** 显式指定 docx4j 配置文件路径（可选），优先级在 Environment 之后、其余文件之前 */
  public static final String CONFIG_FILE_PROPERTY = "bote.dc.docx4j.config.file";
  /** 默认 classpath 配置路径（兜底） */
  public static final String DEFAULT_CLASSPATH_PATH = "config/default/docx4j.properties";
  /** ZSMART_HOME 下 etc 中的 core 配置文件名（可直接在此文件中配置 docx4j.* 键） */
  public static final String CORE_CONFIG_FILE = "coreConfig.properties";
  /** ZSMART_HOME 下 docx4j 专用配置文件名 */
  public static final String DOCX4J_FILENAME = "docx4j.properties";

  private static final String[] LONG_KEYS = {
    "docx4j.openpackaging.parts.MAX_BYTES.unzip.error",
    "docx4j.openpackaging.parts.MAX_BYTES.unmarshal.error",
    "docx4j.openpackaging.package.MAX_UNCOMPRESSED_SIZE.unzip.error"
  };

  private final Environment environment;

  public Docx4jPropertiesInitializer(Environment environment) {
    this.environment = environment;
  }

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public void onApplicationEvent(ApplicationReadyEvent event) {
    try {
      Docx4jProperties.getPropertyLong("docx4j.openpackaging.parts.MAX_BYTES.unzip.error", -1L);
      int fromEnv = applyFromEnvironment();
      int fromFile = applyFromConfigFile();
      if (fromEnv + fromFile > 0) {
        logger.info("docx4j 解压/大小限制配置已注入：Environment {} 项，配置文件 {} 项", fromEnv, fromFile);
      }
    } catch (Exception e) {
      logger.warn("加载并注入 docx4j 外部配置失败: {}", e.getMessage());
    }
  }

  /** 从 Spring Environment 读取并注入，返回注入的 key 数量 */
  private int applyFromEnvironment() {
    int count = 0;
    for (String key : LONG_KEYS) {
      String val = environment.getProperty(key);
      if (StringUtils.isNotBlank(val)) {
        try {
          long num = Long.parseLong(val.trim());
          Docx4jProperties.setPropertyLong(key, num);
          count++;
        } catch (NumberFormatException e) {
          logger.warn("docx4j 配置 {} 无法解析为 long: {}", key, val);
        }
      }
    }
    return count;
  }

  /** 从配置文件补充 Environment 中未提供的 key，返回本次从文件注入的 key 数量 */
  private int applyFromConfigFile() {
    Resource resource = resolveConfigResource();
    if (resource == null || !resource.exists()) {
      return 0;
    }
    Properties loaded = new Properties();
    try (InputStream is = resource.getInputStream();
      InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
      loaded.load(reader);
    }
    catch (Exception e) {
      logger.debug("读取配置文件失败 {}: {}", resource, e.getMessage());
      return 0;
    }
    int count = 0;
    for (String key : LONG_KEYS) {
      if (StringUtils.isNotBlank(environment.getProperty(key))) {
        continue;
      }
      String val = loaded.getProperty(key);
      if (val != null && !val.isBlank()) {
        try {
          long num = Long.parseLong(val.trim());
          Docx4jProperties.setPropertyLong(key, num);
          count++;
        }
        catch (NumberFormatException e) {
          logger.warn("docx4j 配置 {} 无法解析为 long: {}", key, val);
        }
      }
    }
    return count;
  }

  /**
   * 解析配置文件资源顺序：显式路径 > ZSMART_HOME/etc/docx4j.properties > ZSMART_HOME/etc/coreConfig.properties > classpath 默认。
   */
  private Resource resolveConfigResource() {
    String zsmartHome = environment.getProperty("ZSMART_HOME");

    // 1) 显式配置项指定的文件
    String explicitPath = environment.getProperty(CONFIG_FILE_PROPERTY);
    if (StringUtils.isNotBlank(explicitPath)) {
      Path path = Paths.get(explicitPath.trim()).normalize();
      if (Files.isRegularFile(path)) {
        return new FileSystemResource(path.toFile());
      }
      logger.debug("配置项 {} 指定的文件不存在: {}", CONFIG_FILE_PROPERTY, path);
    }

    // 2) ZSMART_HOME/etc/docx4j.properties
    if (StringUtils.isNotBlank(zsmartHome)) {
      Path docx4jInEtc = Paths.get(zsmartHome.trim(), "etc", DOCX4J_FILENAME).normalize();
      if (Files.isRegularFile(docx4jInEtc)) {
        return new FileSystemResource(docx4jInEtc.toFile());
      }
      // 3) ZSMART_HOME/etc/coreConfig.properties（直接从此文件读取 docx4j.* 等键）
      Path coreConfigPath = Paths.get(zsmartHome.trim(), "etc", CORE_CONFIG_FILE).normalize();
      if (Files.isRegularFile(coreConfigPath)) {
        return new FileSystemResource(coreConfigPath.toFile());
      }
    }

    // 4) classpath 默认
    ClassPathResource defaultResource = new ClassPathResource(DEFAULT_CLASSPATH_PATH);
    if (defaultResource.exists()) {
      return defaultResource;
    }
    return null;
  }
}
