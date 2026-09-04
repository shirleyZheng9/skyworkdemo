package com.iwhalecloud.bote.license.enums;

import lombok.Getter;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

/**
 * 版本类型
 *
 * @author zhangJun
 * @since 2022/4/7
 */
@Getter
public enum VersionEnum {
  /** 体验版 */
  TRIAL("trial", "体验版"),
  /** 基础版 */
  BASIC("basic", "基础版"),
  /** 旗舰版 */
  ULTIMATE("ultimate", "旗舰版");

  private final String version;
  private final String versionName;

  VersionEnum(String version, String versionName) {
    this.version = version;
    this.versionName = versionName;
  }

  public static String toVersionName(String version) {
    if (StringUtils.isEmpty(version)) {
      return "";
    }
    return Optional.ofNullable(EnumUtils.getEnum(VersionEnum.class, version.toUpperCase())).map(VersionEnum::getVersionName).orElse("");
  }
}
