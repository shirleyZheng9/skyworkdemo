package com.iwhalecloud.bote.common.diffc.persist.impl;

import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import com.iwhalecloud.bote.dto.portal.UserDTO;
import com.iwhalecloud.bote.mapper.portal.UserManageMapper;
import org.springframework.stereotype.Component;

/**
 * 用户
 *
 * @author qian.sisheng
 * @since 2024/8/12
 */
@Component
public final class UserDifferencePersistence extends BaseRootPersistence<UserDTO> {
  public UserDifferencePersistence(UserManageMapper userManageMapper) {
    setAddConsumer(userManageMapper::insertUser);
    setModifyConsumer(userManageMapper::updateUser);
  }
}
