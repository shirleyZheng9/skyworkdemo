package com.iwhalecloud.bote.portal.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iwhalecloud.bote.dto.portal.LoginInfo;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.collections4.MapUtils;

/**
 * Uportal 检查登录状态响应对象
 *
 * <p>不完整，只包含本工程用到的部分属性</p>
 *
 * @author chen.linfa
 * @since 2024-07-31
 */
@Getter
@Setter
@ToString
public class UportalLoggedResponse {
  /** 响应编码 */
  private String resultCode;
  /** 响应描述 */
  private String resultMsg;
  /** 本地网名称 */
  private String postLanName;
  /** 用户信息 */
  private UserInfo userInfo;
  /** 组织信息 */
  private OrgInfo orgInfo;
  /** 员工信息 */
  private StaffInfo staffInfo;

  /**
   * 是否成功
   */
  @JsonIgnore
  public boolean isSuccess() {
    return "0000".equals(resultCode);
  }

  /**
   * 转换登录信息对象
   */
  public LoginInfo toLoginInfo(String sessionId) {
    LoginInfo loginInfo = new LoginInfo();
    loginInfo.setToken(sessionId);
    loginInfo.setUserId(userInfo.getUserId());
    loginInfo.setUserName(userInfo.getUserCode());
    loginInfo.setRealName(userInfo.getUserName());
    loginInfo.setPhoneNo(userInfo.getPhone());

    Map<String, Object> attributes = new HashMap<>();
    if (MapUtils.isNotEmpty(userInfo.getExtParams())) {
      attributes.putAll(userInfo.getExtParams());
    }
    attributes.put("userInfo", userInfo);
    attributes.put("orgInfo", orgInfo);
    attributes.put("staffInfo", staffInfo);
    loginInfo.setAttributes(attributes);
    return loginInfo;
  }

  /**
   * 用户信息
   */
  @Getter
  @Setter
  @ToString
  public static class UserInfo {
    /** 用户 ID */
    private Long userId;
    /** 用户编码 */
    private String userCode;
    /** 用户名称 */
    private String userName;
    /** 用户手机号 */
    private String phone;
    /** 岗位 ID */
    private Long postId;
    /** 岗位名称 */
    private String postName;
    /** 区域级别 */
    private Long regionLevel;
    /** 上级区域 ID */
    private Long parRegionId;
    /** 区域 ID */
    private Long postRegionId;
    /** 本地网 ID */
    private String postLanId;
    /** 扩展参数 */
    private Map<String, Object> extParams;
  }

  /**
   * 组织信息
   */
  @Getter
  @Setter
  @ToString
  public static class OrgInfo {
    /** 组织 ID */
    private Long orgId;
    /** 组织名称 */
    private String orgName;
    /** 区域 ID */
    private Long regionId;
    /** 区域名称 */
    private String regionName;
  }

  /**
   * 员工信息
   */
  @Getter
  @Setter
  @ToString
  public static class StaffInfo {
    /** 员工 ID */
    private Long staffId;
    /** 员工编码 */
    private String staffCode;
    /** 员工名称 */
    private String staffName;
    /** 岗位 ID */
    private Long partyId;
    /** 组织 ID */
    private Long orgId;
    /** 区域 ID */
    private Long regionId;
  }
}
