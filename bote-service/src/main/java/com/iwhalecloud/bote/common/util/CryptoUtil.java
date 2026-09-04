package com.iwhalecloud.bote.common.util;

import com.iwhalecloud.bss.litchi.crypto.config.FtfKeyProperties;
import com.iwhalecloud.bss.litchi.crypto.ftf.encryptor.FtfRsaEncryptor;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

/**
 * 加解密工具类
 *
 * @author qian.sisheng
 * @since 2024/8/6
 */
public final class CryptoUtil {
  private CryptoUtil() {
  }

  /** RSA 加密工具 */
  private static final FtfRsaEncryptor rsaEncryptor = SpringUtil.getBean(FtfRsaEncryptor.class, () -> new FtfRsaEncryptor(new FtfKeyProperties()));

  /**
   * 加密
   *
   * @param text 明文
   * @return 密文。明文为空时直接返回明文
   */
  @Nullable
  public static String encrypt(@Nullable String text) {
    if (StringUtils.isEmpty(text)) {
      return text;
    }
    try {
      return rsaEncryptor.encrypt(text);
    }
    catch (Exception e) {
      throw BaseErrorConstant.ENCRYPT_ERROR.toException(e);
    }
  }

  /**
   * 解密
   *
   * @param encryptedText 密文
   * @return 明文。解密失败时不报错，返回密文
   */
  @Nullable
  public static String decrypt(@Nullable String encryptedText) {
    // RSA 加密的密文长度至少为 44, 少于这个长度说明不是加密数据
    if (StringUtils.isEmpty(encryptedText) || encryptedText.length() < 44) {
      return encryptedText;
    }
    try {
      return rsaEncryptor.decrypt(encryptedText);
    }
    catch (Exception e) {
      // 解密失败时返回原始字符串，以兼容密文实际为明文数据的情况（比如数据库中的历史数据）
      // 不打印日志、异常，里面可能保护敏感信息
      return encryptedText;
    }
  }

}
