package com.iwhalecloud.bote.common.util;

import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.service.sms.BillSmsClient;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.SpringUtil;

/**
 * 发送SMS短信验证码工具类
 *
 * @author wangtingyun
 * @since 2025-10-15
 */
public final class SendSmsCodeUtil {

  private SendSmsCodeUtil() {
  }

  /**
   * 发送短信验证码
   *
   * @param phoneNo 手段号码
   * @param smsContent 短信内容
   * @return 发送结果
   */
  public static void sendSmsCode(String phoneNo, String smsContent) {
    // 获取短信验证码客户端
    String smsCodeType = SystemParameter.SMS_CODE_PLATFORM.getValueFromDb();
    if ("HenanTelecom".equals(smsCodeType)) {
      sendBillSmsCode(phoneNo, smsContent);
    }
    else {
      throw new BssException("不支持的短信验证码平台类型");
    }
  }

  /**
   * 发送河南电信平台的短信验证码
   */
  private static void sendBillSmsCode(String phoneNo, String smsContent) {
    BillSmsClient billSmsClient = SpringUtil.getBeanOptional(BillSmsClient.class);
    if (billSmsClient == null) {
      throw new BssException("未配置有效的短信服务客户端，请联系管理员");
    }
    boolean sendResult = billSmsClient.sendSmsMessage(phoneNo, smsContent);
    if (!sendResult) {
      throw new BssException("短信发送失败，请联系管理员");
    }
  }

}
