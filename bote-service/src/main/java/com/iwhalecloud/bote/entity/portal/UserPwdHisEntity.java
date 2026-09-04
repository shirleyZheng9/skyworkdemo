package com.iwhalecloud.bote.entity.portal;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 用户密码历史 Entity
 *
 * @author wangtinyun
 * @since 2025-10-17
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_user_pwd_his")
public class UserPwdHisEntity extends BaseEntity {

  @DiffId
  @Schema(description = "主键ID")
  private Long hisId;
  @DiffField(name = "USER_ID")
  @Schema(description = "用户ID")
  private Long userId;
  @DiffField(name = "PASSWORD")
  @Schema(description = "用户密码")
  private String password;

}
