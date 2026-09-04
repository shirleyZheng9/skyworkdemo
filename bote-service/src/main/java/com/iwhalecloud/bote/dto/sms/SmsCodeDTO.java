package com.iwhalecloud.bote.dto.sms;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * 短信验证码信息 DTO
 *
 * @author wangtingyun
 * @since 2025-10-16
 */
@Getter
@Setter
@ToString
public class SmsCodeDTO {

  @Schema(description = "验证码")
  private String smsCode;
  @Schema(description = "验证码请求时间")
  private Date requestTime;

  public SmsCodeDTO(String smsCode) {
    this.smsCode = smsCode;
    this.requestTime = new Date();
  }

}
