package com.iwhalecloud.bote.common.util;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

/**
 * AES 加/解密工具类
 * @author qian.sisheng
 * @since 2024/8/6
 */
@SuppressWarnings("java:S5542")
public final class AesUtil {
  private static final Logger logger = LoggerFactory.getLogger(AesUtil.class);

  private AesUtil() {
  }

  /** 加解密算法/工作模式/填充方式 */
  private static final String KEY_ALGORITHM = "AES";

  /** 默认的加密算法 */
  public static final String DEFAULT_CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";

  @SuppressFBWarnings({"CIPHER_INTEGRITY", "ECB_MODE"})
  @Nullable
  public static String aesEncrypt(String content, String aesKey) {
    try {
      // 创建密码器
      Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);

      byte[] byteContent = content.getBytes(StandardCharsets.UTF_8);

      // 初始化为加密模式的密码器
      cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(aesKey));

      // 加密
      byte[] result = cipher.doFinal(byteContent);

      //通过Base64转码返回
      return Base64.encodeBase64String(result);
    }
    catch (Exception ex) {
      logger.error("AES encrypt error", ex);
    }

    return null;
  }

  /**
   * 生成加密秘钥
   * 转换为AES专用密钥
   * @return SecretKeySpec
   */
  private static SecretKeySpec getSecretKey(final String key) {
    return new SecretKeySpec(Arrays.copyOf(key.getBytes(StandardCharsets.UTF_8), 16), KEY_ALGORITHM);
  }

  @SuppressFBWarnings({"CIPHER_INTEGRITY", "ECB_MODE"})
  @Nullable
  public static String aesDecrypt(String encryptStr, String aesKey) {
    try {
      //实例化
      Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);

      //使用密钥初始化，设置为解密模式
      cipher.init(Cipher.DECRYPT_MODE, getSecretKey(aesKey));

      //执行操作
      byte[] result = cipher.doFinal(Base64.decodeBase64(encryptStr));

      return new String(result, StandardCharsets.UTF_8);
    }
    catch (Exception ex) {
      logger.error("AES decrypt error", ex);
    }
    return null;
  }

  @SuppressFBWarnings({"CIPHER_INTEGRITY", "ECB_MODE"})
  public static byte[] aesDecryptByte(byte[] encryptByte, String hexAesKey) {
    try {
      //实例化
      Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);

      //使用密钥初始化，设置为解密模式
      cipher.init(Cipher.DECRYPT_MODE, getSecretKey(hexAesKey));

      //执行操作
      return cipher.doFinal(Base64.decodeBase64(encryptByte));
    }
    catch (Exception ex) {
      logger.error("AES decrypt error", ex);
    }
    return new byte[0];
  }

}
