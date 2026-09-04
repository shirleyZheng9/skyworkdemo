package com.iwhalecloud.bote.service.security;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.mapper.portal.UserManageMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * 用户详情信息服务，用于Security加载用户信息
 *
 * @author tingyun.wang
 * @since 2025-04-03
 */
@Primary
@Component
@RequiredArgsConstructor
public class BoteUserDetailServiceImpl implements UserDetailsService {

  protected final Logger logger = LoggerFactory.getLogger(getClass());

  private final UserManageMapper userManageMapper;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    boolean exists = userManageMapper.existsUserName(BaseConsts.PORTAL_SYSTEM_CODE_DEFAULT, username);
    if (!exists) {
      logger.error("用户不存在，userName = {}", username);
      throw new UsernameNotFoundException(username + "账号在系统中不存在");
    }
    return new User(username, "", true, true, true, true, new ArrayList<>());
  }

}
