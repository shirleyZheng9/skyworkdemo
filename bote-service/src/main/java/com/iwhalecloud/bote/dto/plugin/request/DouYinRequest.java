package com.iwhalecloud.bote.dto.plugin.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.RandomStringUtils;

/**
 * 抖音请求基础参数
 *
 * @author qian.sisheng
 * @since 2025-07-18
 */
@Setter
@Getter
@ToString
@JsonNaming(SnakeCaseStrategy.class)
@SuppressWarnings("PMD.AvoidUsingHardCodedIP")
public class DouYinRequest {

  /** appId */
  private String aid = "6383";
  /** 版本号 */
  private String versionCode = "170400";
  /** 版本名称 */
  private String versionName = "17.4.0";
  /** 浏览器语言 */
  private String browserLanguage = "zh-CN";
  /** 浏览器平台 */
  private String browserPlatform = "Win32";
  /** 浏览器名称 */
  private String browserName = "Chrome";
  /** 浏览器版本 */
  private String browserVersion = "138.0.0.0";
  /** 引擎名称 */
  private String engineVersion = "138.0.0.0";
  /** 平台 */
  private String platform = "PC";
  /** 抖音msToken */
  private String msToken = RandomStringUtils.insecure().nextAlphanumeric(120);

}
