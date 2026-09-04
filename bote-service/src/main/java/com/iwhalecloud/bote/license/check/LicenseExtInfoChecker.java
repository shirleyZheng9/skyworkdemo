package com.iwhalecloud.bote.license.check;


import com.iwhalecloud.bote.license.entity.LicenseExtInfoDTO;

import jakarta.servlet.http.HttpServletRequest;

/**
 * license 扩展信息检查器接口
 *
 * @author cheng.xu
 */
public interface LicenseExtInfoChecker {
  /**
   * 是否跳过 license 扩展信息检查
   *
   * @param licenseExtInfo license 扩展信息
   * @param request 请求对象
   * @return 是否跳过，默认不跳过
   */
  default boolean skip(LicenseExtInfoDTO licenseExtInfo, HttpServletRequest request) {
    return false;
  }

  /**
   * license 扩展信息检查
   *
   * @param licenseExtInfo license 扩展信息
   * @param request    请求对象
   * @return 检查错误信息
   */
  CheckResult check(LicenseExtInfoDTO licenseExtInfo, HttpServletRequest request);
}
