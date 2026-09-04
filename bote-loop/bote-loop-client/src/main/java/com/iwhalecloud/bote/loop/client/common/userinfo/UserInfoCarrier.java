package com.iwhalecloud.bote.loop.client.common.userinfo;

import com.iwhalecloud.bote.loop.client.evaluation.domain.common.BaseInfoDTO;

public interface UserInfoCarrier {
  BaseInfoDTO getBaseInfo();
  void setBaseInfo(BaseInfoDTO baseInfo);
}
