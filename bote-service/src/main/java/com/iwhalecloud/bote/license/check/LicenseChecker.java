package com.iwhalecloud.bote.license.check;

import com.iwhalecloud.bote.license.cache.LicenseCache;
import com.iwhalecloud.bote.license.entity.LicenseExtInfoDTO;
import com.iwhalecloud.bote.license.util.LicenseParser;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * license 检查器
 *
 * <p>不要使用 {@link com.iwhalecloud.common.licensesdk.LicenseCheckUtil}, 否则检查不通过看不到具体原因，不便排查问题。</p>
 *
 * @author zhangJun
 * @author bianjp
 * @since 2021-12-28
 */
@RequiredArgsConstructor
public class LicenseChecker {
  private static final Logger logger = LoggerFactory.getLogger(LicenseChecker.class);
  /** 默认 ZMP 产品 ID: 低代码开发平台 */
  private static final String DEFAULT_PRODUCT_ID = "1774";
  /** 产品 ID 环境变量，仅用于特殊情况下指定产品 ID */
  private static final String PRODUCT_ID_ENV_VARIABLE = "BOTE_LICENSE_PRODUCT_ID";
  /** 日期格式 */
  private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

  private final LicenseCache licenseCache;

  /**
   * 检查 license
   *
   * @param checkProductId 是否检查 license 的产品 ID，只需要在应用启动和在线更新 license 两种情况下检查，HTTP 拦截器中不需要重复检查
   * @return 检查结果
   */
  public CheckResult checkLicense(boolean checkProductId) {
    LicenseExtInfoDTO licenseExtInfo = licenseCache.getLicense();
    return doCheckLicense(licenseExtInfo, checkProductId);
  }

  /**
   * 检查指定的 license
   *
   * @param license license 内容
   * @return 检查结果
   */
  public CheckResult checkLicense(String license) {
    LicenseExtInfoDTO licenseExtInfo = LicenseParser.parseLicense(license, licenseCache.getPublicKey());
    return doCheckLicense(licenseExtInfo, true);
  }

  /**
   * 执行 license 检查
   */
  private CheckResult doCheckLicense(LicenseExtInfoDTO licenseExtInfo, boolean checkProductId) {
    // 检查有效期
    if (licenseExtInfo.getExpireDate() != null && licenseExtInfo.getExpireDate().getTime() < System.currentTimeMillis()) {
      String msg = "license 已失效, 到期时间: " + DateFormatUtils.format(licenseExtInfo.getExpireDate(), DATE_TIME_FORMAT);
      logger.error(msg);
      return CheckResult.fail(msg);
    }

    // 检查产品 ID, 防止用户使用其它产品的 license
    if (checkProductId) {
      // 需要考虑删除读取环境变量的逻辑，否则用户可以通过设置环境变量绕过 productId 检查
      String expectedProductId = StringUtils.defaultIfEmpty(StringUtils.trim(System.getenv(PRODUCT_ID_ENV_VARIABLE)), DEFAULT_PRODUCT_ID);
      String actualProductId = licenseExtInfo.getProductId();
      if (!expectedProductId.equals(actualProductId)) {
        String msg = String.format("license 产品 ID 不匹配: 应为 %s, 实为 %s", expectedProductId, actualProductId);
        logger.error(msg);
        return CheckResult.fail(msg);
      }
    }

    return CheckResult.success();
  }
}
