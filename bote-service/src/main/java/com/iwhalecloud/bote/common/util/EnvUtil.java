package com.iwhalecloud.bote.common.util;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import lombok.Getter;

/**
 * 环境工具类
 *
 * @author bianjp
 * @since 2024-08-09
 */
@SuppressWarnings("PMD.MissingStaticMethodInNonInstantiatableClass")
public final class EnvUtil {
  private EnvUtil() {
  }

  /** 环境编码配置项 */
  private static final String ENV_CODE_PROPERTY = "bote.env.code";
  /** 环境类型配置项 */
  private static final String ENV_TYPE_PROPERTY = "bote.env.type";
  /** 环境部署模式配置项 */
  private static final String ENV_DEPLOY_TYPE_PROPERTY = "bote.env.deploy.type";
  /** 环境编码 */
  @Getter
  private static final String envCode = SpringUtil.getProperty(ENV_CODE_PROPERTY, "dev");

  /** 环境类型（配置态：Develop 运行态：Operate） */
  @Getter
  private static final String envType = SpringUtil.getProperty(ENV_TYPE_PROPERTY, "Develop");

  /** 运行态环境部署模式（分开部署：separate 合并部署: merge）*/
  @Getter
  private static final String envDeployType = SpringUtil.getProperty(ENV_DEPLOY_TYPE_PROPERTY, "separate");

  public static boolean isDevEnv() {
    return envCode.equals(BaseConsts.ENV_CODE_DEV);
  }

  public static boolean isRuntime() {
    return !envType.equals(BaseConsts.ENV_TYPE_DEV);
  }
}
