package com.iwhalecloud.bote.common.util;

import com.iwhalecloud.bote.common.enums.SystemParameter;
import org.apache.commons.lang3.StringUtils;

/**
 * 字段加密工具类
 *
 * @author tingyun.wang
 * @since 2025-07-10
 */
public final class FieldEncryptUtil {

  /** 加密字段前缀 */
  public static final String ENCRYPT_FIELD_PRE = "{EFP}";

  private FieldEncryptUtil() {
  }

  /**
   * 字段数据加密
   */
  public static String encryptData(String data) {
    // 如果字段数据为空或者已经加密过了的则不加密
    if (StringUtils.isEmpty(data) || data.startsWith(ENCRYPT_FIELD_PRE)) {
      return data;
    }
    // 加密失败则返回原数据
    String aesKey = SystemParameter.ENCRYPTION_AES.getValueFromDb();
    String encryptData = AesUtil.aesEncrypt(data, aesKey);
    return StringUtils.isNotEmpty(encryptData) ? ENCRYPT_FIELD_PRE + encryptData : data;
  }

  /**
   * 解密字段数据
   */
  public static String decryptData(String encryptedData) {
    // 如果字段数据为空或者不是本工具类加密的数据则不解密
    if (StringUtils.isEmpty(encryptedData) || !encryptedData.startsWith(ENCRYPT_FIELD_PRE)) {
      return encryptedData;
    }
    // 如果解密失败则返回原数据
    String aesKey = SystemParameter.ENCRYPTION_AES.getValueFromDb();
    String decryptData = AesUtil.aesDecrypt(encryptedData.substring(StringUtils.length(ENCRYPT_FIELD_PRE)), aesKey);
    return StringUtils.isNotEmpty(decryptData) ? decryptData : encryptedData;
  }

}
