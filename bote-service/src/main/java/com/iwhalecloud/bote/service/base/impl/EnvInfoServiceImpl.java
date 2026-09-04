package com.iwhalecloud.bote.service.base.impl;

import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.util.EnvUtil;
import com.iwhalecloud.bote.common.util.SignUtil;
import com.iwhalecloud.bote.service.base.IEnvInfoService;
import org.springframework.stereotype.Service;

/**
 * 环境信息服务实现类
 *
 * @author qian.sisheng
 * @since 2023/9/25
 */
@Service
public class EnvInfoServiceImpl implements IEnvInfoService {

  @Override
  public String getEnvInfo() {
    String envInfo = SystemParameter.ENV_INFO_TEMPLATE.getValueFromDb();
    envInfo = envInfo.replace(":cookieName", SignUtil.getCookieName());
    envInfo = envInfo.replace(":userIdCookie", SignUtil.getUserIdCookieName());
    envInfo = envInfo.replace(":serverTime", String.valueOf(System.currentTimeMillis()));
    envInfo = envInfo.replace(":securityMode", SystemParameter.SIGN_SECURITY_MODE.getValueFromDb());
    envInfo = envInfo.replace(":envType", EnvUtil.getEnvType());
    envInfo = envInfo.replace(":envDeployType", EnvUtil.getEnvDeployType());
    // 启用 nodejs 时，增加返回 $.lxNodejsEnabled = true
    if (SystemParameter.NODE_JS_ENABLED.getBooleanValueFromDb()) {
      int lastIndex = envInfo.lastIndexOf("}");
      StringBuilder sb = new StringBuilder();
      String subEnv = envInfo.substring(0, lastIndex);
      sb.append(subEnv);
      if (!subEnv.trim().endsWith(";")) {
        sb.append(";");
      }
      sb.append("    $.btNodejsEnabled = true");
      sb.append("\n");
      sb.append(envInfo.substring(lastIndex));
      return sb.toString();
    }
    return envInfo;
  }
}
