package com.iwhalecloud.bote.license.check;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.license.condition.ConditionalOnLicense;
import com.iwhalecloud.bote.license.entity.LicenseExtInfoDTO;
import com.iwhalecloud.bote.license.service.LicenseCheckService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户数检查
 *
 * @author cheng.xu
 */
@Component
@RequiredArgsConstructor
@ConditionalOnLicense
public class UserNumChecker implements LicenseExtInfoChecker, InitializingBean {

  private final LicenseCheckService licenseCheckService;

  /** 需要校验的接口列表 */
  private static final List<String> NEED_CHECK_URLS = new ArrayList<>();

  private String prefix = BaseConsts.API_PREFIX;

  @Override
  public void afterPropertiesSet() {
    // 确保 URL 以斜杠开头, 以斜杠结尾
    prefix = Strings.CS.appendIfMissing(prefix, "/");
    prefix = "/" + StringUtils.stripStart(prefix, "/");
    // 注册用户接口
    NEED_CHECK_URLS.add(prefix + "manager/user/registerUser");
    // 保存用户接口：该接口新增和编辑都用一个接口，无法区分做区分
    NEED_CHECK_URLS.add(prefix + "manager/user/saveUser");
  }

  @Override
  public CheckResult check(LicenseExtInfoDTO licenseExt, HttpServletRequest request) {
    // url不在校验列表中则不需要校验
    String reqUrl = request.getRequestURI();
    if (!NEED_CHECK_URLS.contains(reqUrl)) {
      return CheckResult.success();
    }
    // 校验当前最大有效用户数
    Long maxUserNum = licenseExt.getMaxUserNum();
    return licenseCheckService.checkUserNum(maxUserNum);
  }

  @Override
  public boolean skip(LicenseExtInfoDTO licenseExt, HttpServletRequest request) {
    // 空值或非正数表示不限制
    return licenseExt.getMaxUserNum() == null || licenseExt.getMaxUserNum() <= 0;
  }
}
