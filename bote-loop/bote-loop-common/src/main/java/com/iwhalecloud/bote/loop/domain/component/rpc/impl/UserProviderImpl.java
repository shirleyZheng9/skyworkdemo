package com.iwhalecloud.bote.loop.domain.component.rpc.impl;

import com.iwhalecloud.bote.loop.domain.component.rpc.IUserProvider;
import com.iwhalecloud.bote.loop.domain.component.rpc.dto.RpcUserInfo;
import com.iwhalecloud.bote.mapper.portal.UserManageMapper;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserProviderImpl implements IUserProvider {
  private final UserManageMapper userManageMapper;

  @Override
  public List<RpcUserInfo> mGetUserInfo(List<String> userIds) {

    return userIds.stream()
      .map(Long::parseLong)
      .map(o -> userManageMapper.getUser(o)).filter(Objects::nonNull).map(userPO -> {
        return RpcUserInfo.builder()
          .userId(String.valueOf(userPO.getUserId()))
          // 展示名优先取 real_name：天工用户的 user_name 存的是外部用户 ID
          .userName(StringUtils.isNotBlank(userPO.getRealName()) ? userPO.getRealName() : userPO.getUserName())
          .build();
      })
      .toList();
  }
}
