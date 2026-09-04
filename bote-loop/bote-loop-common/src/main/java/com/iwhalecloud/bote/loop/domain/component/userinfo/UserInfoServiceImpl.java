package com.iwhalecloud.bote.loop.domain.component.userinfo;

import com.google.common.collect.Maps;
import com.iwhalecloud.bote.loop.client.common.userinfo.UserInfoCarrier;
import com.iwhalecloud.bote.loop.client.common.userinfo.UserInfoService;
import com.iwhalecloud.bote.loop.client.evaluation.domain.common.BaseInfoDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.common.UserInfoDTO;
import com.iwhalecloud.bote.loop.domain.component.rpc.IUserProvider;
import com.iwhalecloud.bote.loop.domain.component.rpc.dto.RpcUserInfo;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@AllArgsConstructor
public class UserInfoServiceImpl implements UserInfoService {

  private final IUserProvider userProvider;

  @Override
  public void packUserInfo(List<UserInfoCarrier> userInfoCarriers) {
    List<String> userIds = getUserIds(userInfoCarriers);
    if (CollectionUtils.isEmpty(userIds)) {
      return;
    }
    List<RpcUserInfo> userInfos = userProvider.mGetUserInfo(userIds);
    Map<String, RpcUserInfo> userInfoMap = Maps.newHashMap();
    for (RpcUserInfo userInfo : userInfos) {
      userInfoMap.put(userInfo.getUserId(), userInfo);
    }
    for (UserInfoCarrier userInfoCarrier : userInfoCarriers) {
      BaseInfoDTO baseInfo = userInfoCarrier.getBaseInfo();
      if (baseInfo == null) {
        continue;
      }
      UserInfoDTO createdBy = baseInfo.getCreatedBy();
      setCreatedByInfo(userInfoMap, createdBy);
      UserInfoDTO updatedBy = baseInfo.getUpdatedBy();
      setUpdatedByInfo(userInfoMap, updatedBy);
    }
  }

  @Override
  public <E> void packUserInfoList(List<E> dataList) {
    List<UserInfoCarrier> userInfoCarriers = Lists.newArrayList();
    for (E data : dataList) {
      if (data instanceof UserInfoCarrier) {
        UserInfoCarrier carrier = (UserInfoCarrier) data;
        userInfoCarriers.add(carrier);
      }
    }
    packUserInfo(userInfoCarriers);
  }

  @Override
  public void packUserInfoObj(Object data) {
    List<UserInfoCarrier> userInfoCarriers = Lists.newArrayList();
    if (data instanceof UserInfoCarrier) {
      UserInfoCarrier carrier = (UserInfoCarrier) data;
      userInfoCarriers.add(carrier);
    }
    packUserInfo(userInfoCarriers);
  }

  private List<String> getUserIds(List<UserInfoCarrier> userInfoCarriers) {
    List<String> userIds = Lists.newArrayList();
    for (UserInfoCarrier userInfoCarrier : userInfoCarriers) {
      BaseInfoDTO baseInfo = userInfoCarrier.getBaseInfo();
      if (baseInfo == null) {
        continue;
      }
      UserInfoDTO createdBy = baseInfo.getCreatedBy();
      if (createdBy != null && StringUtils.isNotBlank(createdBy.getUserId())) {
        userIds.add(createdBy.getUserId());
      }
      UserInfoDTO updatedBy = baseInfo.getUpdatedBy();
      if (updatedBy != null && StringUtils.isNotBlank(updatedBy.getUserId())) {
        userIds.add(updatedBy.getUserId());
      }
    }
    return userIds.stream().distinct().toList();
  }

  private void setCreatedByInfo(Map<String, RpcUserInfo> userInfoMap, UserInfoDTO createdBy) {
    if (createdBy != null && StringUtils.isNotBlank(createdBy.getUserId())) {
      RpcUserInfo rpcUserInfo = userInfoMap.get(createdBy.getUserId());
      createdBy.setName(rpcUserInfo.getUserName());
    }
  }

  private void setUpdatedByInfo(Map<String, RpcUserInfo> userInfoMap, UserInfoDTO updatedBy) {
    if (updatedBy != null && StringUtils.isNotBlank(updatedBy.getUserId())) {
      RpcUserInfo rpcUserInfo = userInfoMap.get(updatedBy.getUserId());
      updatedBy.setName(rpcUserInfo.getUserName());
    }
  }
}
