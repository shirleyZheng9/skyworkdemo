package com.iwhalecloud.bote.llm.client.util;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 模型配置校验工具类
 *
 * @author bianjp
 * @since 2025-05-13
 */
public final class ModelValidationUtil {
  private ModelValidationUtil() {
  }

  /**
   * 校验 URL，不合法时抛异常
   *
   * @param url 接口地址
   */
  public static void validateUrl(@Nullable String url, String title) {
    Assert.isTrue(StringUtils.isNotEmpty(url), () -> title + "不能为空");
    //noinspection HttpUrlsUsage
    Assert.isTrue(url.startsWith("http://") || url.startsWith("https://"), () -> title + "必须以 http:// 或 https:// 开头");
    try {
      new URI(url).toURL();
    }
    catch (URISyntaxException | MalformedURLException e) {
      throw new IllegalArgumentException(title + "不合法", e);
    }
  }
}
