package com.iwhalecloud.bote.license.util;

import com.iwhalecloud.bote.license.enums.LicenseErrorConst;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * license 文件读取器
 *
 * <p>目前只会在应用启动时使用，读取失败时直接抛异常让应用启动失败。</p>
 *
 * @author zhangJun
 * @since 2022/4/7
 */
@SuppressWarnings("java:S2139")
public final class LicenseFileReader {
  private static final Logger logger = LoggerFactory.getLogger(LicenseFileReader.class);

  /** 默认公钥 */
  private static final String DEFAULT_PUBLIC_KEY = SpringUtil.getRequiredProperty("bote.license.public.key");
  /** license 文件路径配置项。未配置时默认取 ZSMART_HOME/etc 目录下的 {@link #LICENSE_FILE_NAME} */
  private static final String LICENSE_FILE_CONFIG = "bote.license.file";
  /** license 文件名称 */
  private static final String LICENSE_FILE_NAME = "bote.lic";
  /** 公钥文件路径配置项。未配置时默认取 ZSMART_HOME/etc 目录下的 {@link #PUBLIC_KEY_FILE_NAME} */
  private static final String PUBLIC_KEY_FILE_CONFIG = "bote.license.keyFile";
  /** 公钥文件名称 */
  private static final String PUBLIC_KEY_FILE_NAME = "pubkey.pem";

  private LicenseFileReader() {
  }

  /**
   * 从文件读取 license 内容
   *
   * @return license 内容
   */
  public static String readLicenseStr() {
    Resource resource = getResource(LICENSE_FILE_CONFIG, LICENSE_FILE_NAME);
    if (!resource.exists()) {
      throw LicenseErrorConst.LICENSE_FILE_NOT_FOUND.toException();
    }
    logger.info("Reading license from {}", resource);
    try (InputStream inputStream = resource.getInputStream()) {
      String content = IOUtils.toString(inputStream, StandardCharsets.UTF_8).trim();
      if (StringUtils.isEmpty(content)) {
        throw LicenseErrorConst.LICENSE_FILE_EMPTY.toException();
      }
      return content;
    }
    catch (IOException e) {
      logger.error("Failed to read license file: file={}", resource, e);
      throw LicenseErrorConst.LICENSE_FILE_READ_FAILED.toException(e, e.getMessage());
    }
  }

  /**
   * 从文件读取并解析公钥
   *
   * @return 公钥
   */
  public static PublicKey readPublicKey() {
    return parsePublicKey(readPublicKeyStr());
  }

  /**
   * 从文件读取公钥内容
   *
   * @return 公钥字符串
   */
  private static String readPublicKeyStr() {
    Resource resource = getResource(PUBLIC_KEY_FILE_CONFIG, PUBLIC_KEY_FILE_NAME);
    if (!resource.exists()) {
      logger.warn("No public key file found, using default key");
      return DEFAULT_PUBLIC_KEY;
    }
    logger.info("Reading public key from {}", resource);
    try (InputStream inputStream = resource.getInputStream()) {
      String content = IOUtils.toString(inputStream, StandardCharsets.UTF_8).trim();
      if (StringUtils.isEmpty(content)) {
        throw LicenseErrorConst.PUBLIC_KEY_FILE_EMPTY.toException();
      }
      return content;
    }
    catch (IOException e) {
      logger.error("Failed to read public key file: file={}", resource, e);
      throw LicenseErrorConst.PUBLIC_KEY_FILE_READ_FAILED.toException(e, e.getMessage());
    }
  }

  /**
   * 获取资源
   */
  private static Resource getResource(String configProperty, String filename) {
    Environment environment = SpringUtil.getEnvironment();
    // 优先取配置项指定的文件路径
    String filePath = environment.getProperty(configProperty);
    if (StringUtils.isNotEmpty(filePath)) {
      // 支持指定 classpath 路径（比如文件放在定制包）
      if (filePath.startsWith(ResourceUtils.CLASSPATH_URL_PREFIX)) {
        return new ClassPathResource(filePath.substring(ResourceUtils.CLASSPATH_URL_PREFIX.length()));
      }
      return new FileSystemResource(filePath);
    }
    // 其次取 ZSMART_HOME/etc 目录下的文件
    String zsmartHome = environment.getProperty("ZSMART_HOME");
    if (StringUtils.isNotEmpty(zsmartHome)) {
      return new FileSystemResource(Paths.get(zsmartHome, "etc", filename));
    }
    // 兼容将文件放在 jar 包中的情况
    return new ClassPathResource(filename);
  }

  /**
   * 解析公钥
   *
   * <p>基于 {@link com.iwhalecloud.common.license.core.util.RSAUtil#convertToPublicKey}</p>
   */
  @SuppressWarnings("JavadocReference")
  private static PublicKey parsePublicKey(String publicKeyStr) {
    byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyStr);
    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);

    try {
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      return keyFactory.generatePublic(keySpec);
    }
    catch (Exception e) {
      logger.error("Failed to parse public key: str={}", publicKeyStr, e);
      throw LicenseErrorConst.PUBLIC_KEY_PARSE_FAILED.toException(e, e.getMessage());
    }
  }
}
