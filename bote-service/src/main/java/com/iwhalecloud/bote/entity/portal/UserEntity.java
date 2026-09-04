package com.iwhalecloud.bote.entity.portal;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.iwhalecloud.bote.common.annotation.EncryptField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * 用户 Entity
 *
 * @author auto
 * @since 2024-09-13
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_user")
@EncryptField
public class UserEntity extends BaseEntity {
  @DiffId
  @Schema(description = "主键")
  private Long userId;
  @DiffField(name = "USER_NAME")
  @Schema(description = "用户名")
  private String userName;
  @DiffField(name = "REAL_NAME")
  @Schema(description = "用户姓名")
  private String realName;
  @DiffField(name = "USER_TYPE")
  @Schema(description = "用户类型: 平台管理员:10  项目开发人员:20  使用者人员:30")
  private String userType;
  @DiffField(name = "PHONE_NO")
  @Schema(description = "手机号")
  @EncryptField
  private String phoneNo;
  @DiffField(name = "EMAIL")
  @Schema(description = "邮箱")
  @EncryptField
  private String email;
  @DiffField(name = "PASSWORD")
  @Schema(description = "密码")
  private String password;
  @DiffField(name = "DEFAULT_TENANT_ID")
  @Schema(description = "默认租户ID")
  private Long defaultTenantId;
  @DiffField(name = "USER_ICON")
  @Schema(description = "头像")
  private String userIcon;
  @DiffField(name = "SYSTEM_CODE")
  @Schema(description = "系统编码")
  private String systemCode;
  @DiffField(name = "EXT_USER_ID")
  @Schema(description = "外系统用户 ID")
  private String extUserId;
  @DiffField(name = "CREATE_TENANT_ID")
  @Schema(description = "创建租户ID")
  private Long createTenantId;
  @DiffField(name = "IS_LOCKED")
  @Schema(description = "是否锁定")
  private String isLocked;
  @DiffField(name = "LOGIN_FAIL_COUNT")
  @Schema(description = "登录错误次数")
  private Integer loginFailCount;
  @DiffField(name = "USER_STATE")
  @Schema(description = "用户状态")
  private String userState;
  @DiffField(name = "USER_EXP_DATE")
  @Schema(description = "账号失效日期")
  @JsonFormat(pattern = "yyyy-MM-dd", locale = "zh", timezone = "GMT+8")
  private Date userExpDate;
  @DiffField(name = "PWD_UPDATED_TIME")
  @Schema(description = "密码修改日期")
  private Date pwdUpdatedTime;
}
