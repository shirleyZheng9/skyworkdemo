package com.iwhalecloud.bote.license.service;

import com.iwhalecloud.bote.license.check.CheckResult;

/**
 * license校验服务
 *
 * @author tingyun.wang
 * @since 2025-05-30
 */
public interface LicenseCheckService {

  /**
   * 检验用户数量
   *
   * @param maxUser 最大用户数
   * @return 结果
   */
  CheckResult checkUserNum(long maxUser);

}
