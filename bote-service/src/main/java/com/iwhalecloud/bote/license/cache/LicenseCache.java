package com.iwhalecloud.bote.license.cache;

import com.iwhalecloud.bote.license.entity.LicenseExtInfoDTO;
import com.iwhalecloud.bote.license.util.LicenseFileReader;
import com.iwhalecloud.bote.license.util.LicenseParser;
import java.security.PublicKey;
import lombok.Getter;

/**
 * license 缓存
 *
 * @author cheng.xu
 * @author bianjp
 * @since 2022-12-30
 */
public class LicenseCache {
  /** 公钥 */
  @Getter
  private final PublicKey publicKey;
  /** license 扩展信息 */
  @Getter
  private final LicenseExtInfoDTO license;

  public LicenseCache() {
    String licenseContent = LicenseFileReader.readLicenseStr();
    // 应用启动时从配置文件读取公钥和 license, 不取分布式缓存
    this.publicKey = LicenseFileReader.readPublicKey();
    this.license = LicenseParser.parseLicense(licenseContent, publicKey);
  }

}
