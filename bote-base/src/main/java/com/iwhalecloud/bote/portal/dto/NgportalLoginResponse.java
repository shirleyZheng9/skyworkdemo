package com.iwhalecloud.bote.portal.dto;

import com.iwhalecloud.bote.dto.portal.LoginInfo;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.collections4.MapUtils;

/**
 * ngportal 登录响应
 *
 * @author chen.linfa
 * @since 2024-07-31
 */
@Getter
@Setter
@ToString
public class NgportalLoginResponse {
  /** 用户 ID */
  private Long userId;
  /** 用户名 */
  private String userCode;
  /** 名称 */
  private String userName;
  /** 用户手机号 */
  private String phone;
  /** 扩展属性 */
  private Map<String, Object> attributes;

  /**
   * 转换登录信息对象
   */
  public LoginInfo toLoginInfo(String sessionId) {
    LoginInfo loginInfo = new LoginInfo();
    loginInfo.setToken(sessionId);
    loginInfo.setUserId(userId);
    loginInfo.setUserName(userCode);
    loginInfo.setRealName(userName);
    loginInfo.setPhoneNo(phone);
    loginInfo.setAttributes(MapUtils.emptyIfNull(attributes));
    return loginInfo;
  }
}
