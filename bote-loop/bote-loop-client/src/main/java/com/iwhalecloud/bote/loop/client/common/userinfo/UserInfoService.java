package com.iwhalecloud.bote.loop.client.common.userinfo;

import java.util.List;

public interface UserInfoService {
  void packUserInfo(List<UserInfoCarrier> userInfoCarriers);

  <E> void packUserInfoList(List<E> dataList);

  void packUserInfoObj(Object data);

}
