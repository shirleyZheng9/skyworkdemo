package com.iwhalecloud.bote.dto.portal;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.iwhalecloud.bote.common.annotation.EncryptField;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 简单用户信息
 *
 * <p>用作列表查询、分页查询的查询结果。</p>
 *
 * @author bianjp
 * @since 2024-12-03
 */
@Getter
@Setter
@ToString
@Schema(description = "简单用户信息")
@EncryptField
public class SimpleUserDTO {
  /** 用户 ID */
  private Long userId;
  /** 用户名 */
  private String userName;
  /** 用户姓名 */
  private String realName;
  /** 手机号 */
  @EncryptField
  private String phoneNo;
  /** 邮箱 */
  @EncryptField
  private String email;
  /** 备注 */
  private String remark;
  /** 系统编码 */
  private String systemCode;
  /** 创建时间 */
  private Date createdTime;
  /** 用户角色 */
  private String userRole;
  /** 租户成员列表 */
  private List<TenantUserDTO> tenantUserList;
  /** 是否锁定 */
  private String isLocked;
  /** 用户状态 */
  private String userState;
  /** 账号失效日期 */
  @JsonFormat(pattern = "yyyy-MM-dd", locale = "zh", timezone = "GMT+8")
  private Date userExpDate;
}
