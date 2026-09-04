package com.iwhalecloud.bote.common.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import org.apache.commons.codec.binary.Base64;
import org.junit.jupiter.api.Test;

/**
 * {@link AesUtil} 单元测试
 *
 * <p>覆盖 AES/ECB/PKCS5Padding 加解密往返、字节数组解密以及异常分支（null 输入、非法密文）。
 * 私有方法 {@code getSecretKey} 通过公有方法间接覆盖。</p>
 */
class AesUtilTest {

  private static final String KEY = "1234567890abcdef"; // 16 字节密钥

  @Test
  void aesEncryptDecrypt_roundTrip_returnsOriginal() {
    String content = "hello 世界";
    String encrypted = AesUtil.aesEncrypt(content, KEY);
    assertThat(encrypted).isNotNull().isNotEqualTo(content);

    String decrypted = AesUtil.aesDecrypt(encrypted, KEY);
    assertThat(decrypted).isEqualTo(content);
  }

  @Test
  void aesEncrypt_differentKey_producesDifferentCipher() {
    String content = "hello";
    String encrypted1 = AesUtil.aesEncrypt(content, KEY);
    String encrypted2 = AesUtil.aesEncrypt(content, "abcdef1234567890");
    assertThat(encrypted1).isNotEqualTo(encrypted2);
  }

  @Test
  void aesEncrypt_nullContent_returnsNull() {
    assertThat(AesUtil.aesEncrypt(null, KEY)).isNull();
  }

  @Test
  void aesDecrypt_invalidCipher_returnsNull() {
    // 合法 Base64 但非有效密文，doFinal 抛异常后被捕获
    String invalidCipher = Base64.encodeBase64String(new byte[] {1, 2, 3, 4, 5});
    assertThat(AesUtil.aesDecrypt(invalidCipher, KEY)).isNull();
  }

  @Test
  void aesDecryptByte_roundTrip_returnsOriginalBytes() {
    String content = "byte content";
    String encrypted = AesUtil.aesEncrypt(content, KEY);
    assertThat(encrypted).isNotNull();

    byte[] result = AesUtil.aesDecryptByte(encrypted.getBytes(StandardCharsets.UTF_8), KEY);
    assertThat(result).isEqualTo(content.getBytes(StandardCharsets.UTF_8));
  }

  @Test
  void aesDecryptByte_invalidInput_returnsEmptyArray() {
    byte[] result = AesUtil.aesDecryptByte("invalid".getBytes(StandardCharsets.UTF_8), KEY);
    assertThat(result).isEmpty();
  }

  @Test
  void aesDecrypt_withShortKey_padsTo16Bytes() {
    // 短于 16 字节的密钥会被 Arrays.copyOf 填充为 0
    String content = "test";
    String encrypted = AesUtil.aesEncrypt(content, "short");
    assertThat(encrypted).isNotNull();
    assertThat(AesUtil.aesDecrypt(encrypted, "short")).isEqualTo(content);
  }
}
