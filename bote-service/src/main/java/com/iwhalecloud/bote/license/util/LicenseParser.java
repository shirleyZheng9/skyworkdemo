package com.iwhalecloud.bote.license.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.collect.ImmutableList;
import com.iwhalecloud.bote.license.entity.LicenseExtInfoDTO;
import com.iwhalecloud.bote.license.enums.LicenseErrorConst;
import com.iwhalecloud.bote.license.enums.VersionEnum;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.common.license.core.model.LicenseInfo;
import com.iwhalecloud.common.license.core.util.AESUtil;
import com.iwhalecloud.common.license.core.util.LicenseUtil;
import com.iwhalecloud.common.license.core.util.RSAUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;

/**
 * license 解析器
 *
 * @author bianjp
 * @since 2023-06-06
 */
public final class LicenseParser {
  private static final Logger logger = LoggerFactory.getLogger(LicenseParser.class);
  private static final ObjectMapper xmlMapper = XmlMapper.builder().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).build();

  private LicenseParser() {
  }

  /**
   * 解析 license
   *
   * <p>基于 {@link com.iwhalecloud.common.licensesdk.LicenseCheckUtil#checkLicense} 优化，解析失败时在异常信息和日志中记录具体原因</p>
   *
   * @param license license 内容
   * @param publicKey 公钥
   * @return license 扩展信息
   */
  public static LicenseExtInfoDTO parseLicense(String license, PublicKey publicKey) {
    license = StringUtils.trim(license);
    Assert.hasLength(license, "license 不能为空");
    Assert.notNull(publicKey, "publicKey 不能为空");
    String[] pieces = StringUtils.split(license, "&", 2);
    if (pieces.length != 2) {
      logger.error("Invalid license: {}", license);
      throw LicenseErrorConst.LICENSE_INVALID.toException("没有 \"&\"");
    }
    // AES 加密的 license 信息
    String encryptedContent = pieces[0];
    // RSA 签名，用于防篡改
    String signature = pieces[1];

    // 校验签名，防篡改
    verifySignature(encryptedContent, signature, publicKey);

    // 解析 license
    String licenseContent = AESUtil.aES128Decrypt(encryptedContent);
    LicenseInfo licenseInfo = LicenseUtil.convertStringToLicense(licenseContent);
    if (licenseInfo == null) {
      logger.error("Invalid license, no attributes found: {}", license);
      throw LicenseErrorConst.LICENSE_INVALID.toException("属性为空");
    }

    // 解析 license 扩展信息
    Map<String, Object> extMap = parseExtendInfo(StringUtils.trim(licenseInfo.getExtendInfo()));
    String version = MapUtils.getString(extMap, "version");
    Long maxUser = MapUtils.getLong(extMap, "maxuser");
    Long maxApp = MapUtils.getLong(extMap, "maxApp");
    String databaseIp = MapUtils.getString(extMap, "dataBaseIP", StringUtils.EMPTY);

    LicenseExtInfoDTO licenseExtInfo = new LicenseExtInfoDTO(licenseInfo);
    licenseExtInfo.setVersion(version);
    licenseExtInfo.setVersionName(VersionEnum.toVersionName(version));
    licenseExtInfo.setMaxUserNum(maxUser);
    licenseExtInfo.setDatabaseIps(ImmutableList.copyOf(StringUtils.splitByWholeSeparator(databaseIp, ",")));
    licenseExtInfo.setMaxAppNum(maxApp);

    return licenseExtInfo;
  }

  /**
   * 解析 license 的扩展信息属性(xml 格式)
   */
  @SuppressWarnings({"unchecked", "java:S2139"})
  private static Map<String, Object> parseExtendInfo(String extendInfoXml) {
    if (StringUtils.isEmpty(extendInfoXml)) {
      return Collections.emptyMap();
    }
    try {
      // 示例内容: <lic><ext><version>ultimate</version><maxuser></maxuser></ext></lic>
      Map<String, Object> map = xmlMapper.readValue(extendInfoXml, new TypeReference<Map<String, Object>>() {
      });
      return (Map<String, Object>) MapUtils.getMap(map, "ext");
    }
    catch (JsonProcessingException e) {
      logger.error("Failed to parse license ext info: {}", extendInfoXml, e);
      throw LicenseErrorConst.LICENSE_EXTEND_INFO_PARSE_FAILED.toException(e, e.getMessage());
    }
  }

  /**
   * 检查 license 的签名合法性
   *
   * <p>基于 {@link RSAUtil#verify} 优化</p>
   */
  @SuppressWarnings({"PMD.AvoidRethrowingException", "java:S2139"})
  @SuppressFBWarnings("WEAK_MESSAGE_DIGEST_MD5")
  private static void verifySignature(String license, String signature, PublicKey publicKey) {
    try {
      Signature sig = Signature.getInstance("MD5withRSA");
      sig.initVerify(publicKey);
      sig.update(license.getBytes(StandardCharsets.UTF_8));
      if (!sig.verify(Base64.getDecoder().decode(signature))) {
        logger.error("License signature not match: license={}, signature={}", license, signature);
        throw LicenseErrorConst.LICENSE_SIGNATURE_INVALID.toException();
      }
    }
    catch (BssException e) {
      throw e;
    }
    catch (Exception e) {
      logger.error("Failed to verify license signature: license={}, signature={}", license, signature, e);
      throw LicenseErrorConst.LICENSE_SIGNATURE_FAILED.toException(e, e.getMessage());
    }
  }
}
