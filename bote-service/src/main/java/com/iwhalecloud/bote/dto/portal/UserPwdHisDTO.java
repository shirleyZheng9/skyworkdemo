package com.iwhalecloud.bote.dto.portal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.iwhalecloud.bote.entity.portal.UserPwdHisEntity;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 用户密码历史 DTO
 *
 * @author wangtinyun
 * @since 2025-10-17
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_user_pwd_his")
@JsonInclude(Include.NON_NULL)
public class UserPwdHisDTO extends UserPwdHisEntity {

}
