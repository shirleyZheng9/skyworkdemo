package com.iwhalecloud.bote.common.util;

import com.iwhalecloud.bote.common.enums.SystemParameter;
import org.apache.commons.lang3.StringUtils;

/**
 * 数据脱敏工具类
 *
 * @author wang.tingyun
 * @since 2025-08-20
 */
public final class DataMaskUtil {

  private DataMaskUtil() {
  }

  /**
   * 手机号码脱敏
   *
   * @param phoneNo 手机号码
   * @return 脱敏后的手机号码
   */
  public static String maskPhone(String phoneNo) {
    if (StringUtils.isEmpty(phoneNo)) {
      return phoneNo;
    }

    String sensitiveRule = SystemParameter.MASK_PHONE_RULE.getValueFromDb();
    switch (sensitiveRule) {
      case "1":
        // 不脱敏
        return phoneNo;
      case "2":
        // 完全脱敏
        return StringUtils.repeat("*", phoneNo.length());
      case "3":
        // 保留前3位和后4位
        return maskPhoneKeepBoth(phoneNo);
      default:
        // 默认不脱敏
        return phoneNo;
    }
  }

  /**
   * 邮箱脱敏
   *
   * @param email 邮箱地址
   * @return 脱敏后的邮箱地址
   */
  public static String maskEmail(String email) {
    if (StringUtils.isEmpty(email)) {
      return email;
    }

    String sensitiveRule = SystemParameter.MASK_EMAIL_RULE.getValueFromDb();
    switch (sensitiveRule) {
      case "1":
        // 不脱敏
        return email;
      case "2":
        // 完全脱敏
        return StringUtils.repeat("*", email.length());
      case "3":
        // 保留第1位和"@"及后面内容
        return maskEmailKeepFirstAndDomain(email);
      default:
        // 默认不脱敏
        return email;
    }
  }

  /**
   * 手机号码脱敏 - 保留前3位和后4位
   *
   * @param phoneNo 手机号码
   * @return 脱敏后的手机号码
   */
  public static String maskPhoneKeepBoth(String phoneNo) {
    if (phoneNo.length() <= 7) {
      return phoneNo;
    }
    
    String prefix = phoneNo.substring(0, 3);
    String suffix = phoneNo.substring(phoneNo.length() - 4);
    String middle = StringUtils.repeat("*", phoneNo.length() - 7);
    
    return prefix + middle + suffix;
  }

  /**
   * 邮箱脱敏 - 保留第1位和"@"及后面内容
   *
   * @param email 邮箱地址
   * @return 脱敏后的邮箱地址
   */
  private static String maskEmailKeepFirstAndDomain(String email) {
    int atIndex = email.indexOf('@');
    if (atIndex <= 0) {
      // 如果没有@符号或@在开头，则完全脱敏
      return StringUtils.repeat("*", email.length());
    }
    
    String localPart = email.substring(0, atIndex);
    String domainPart = email.substring(atIndex);
    
    if (localPart.length() == 1) {
      // 如果@前只有1位，则保留
      return localPart + domainPart;
    }
    
    // 保留第1位，其余用*代替
    String desensitizedLocal = localPart.charAt(0) + StringUtils.repeat("*", localPart.length() - 1);
    
    return desensitizedLocal + domainPart;
  }
}
