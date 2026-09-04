package com.iwhalecloud.bote.license.service.impl;

import com.iwhalecloud.bote.license.check.CheckResult;
import com.iwhalecloud.bote.license.enums.LicenseErrorConst;
import com.iwhalecloud.bote.license.service.LicenseCheckService;
import com.iwhalecloud.bote.mapper.portal.UserManageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * license校验服务
 *
 * @author tingyun.wang
 * @since 2025-05-30
 */
@Service
@RequiredArgsConstructor
public class LicenseCheckServiceImpl implements LicenseCheckService {

  private final UserManageMapper userManageMapper;

  @Override
  public CheckResult checkUserNum(long maxUser) {
    long userNum = userManageMapper.countAllUser();
    if (userNum > maxUser) {
      return CheckResult.fail(LicenseErrorConst.USER_NUM_EXCEEDED_LIMIT, userNum, maxUser);
    }
    return CheckResult.success();
  }
}
