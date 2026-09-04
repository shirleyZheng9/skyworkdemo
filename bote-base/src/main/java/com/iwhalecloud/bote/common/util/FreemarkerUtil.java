package com.iwhalecloud.bote.common.util;

import com.iwhalecloud.bss.litchi.base.exception.BssException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import freemarker.template.Configuration;
import freemarker.template.Template;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * FreeMarker 工具类
 *
 * @author bianjp
 * @since 2024-08-16
 */
public final class FreemarkerUtil {
  private static final Logger logger = LoggerFactory.getLogger(FreemarkerUtil.class);
  /** FreeMarker 配置实例 */
  private static final Configuration configuration = buildFreemarkerConfiguration();

  private FreemarkerUtil() {
  }

  /**
   * 处理模板
   *
   * @param template 模板内容
   * @param variables 变量
   * @return 处理结果
   */
  @SuppressFBWarnings({"TEMPLATE_INJECTION_FREEMARKER", "CRLF_INJECTION_LOGS"})
  public static String process(String template, @Nullable Object variables) {
    Assert.hasLength(template, "template 不能为空");
    try {
      Template templateInstance = new Template("template", template, configuration);
      StringWriter writer = new StringWriter(template.length());
      templateInstance.process(variables, writer);
      return writer.toString();
    }
    catch (Exception e) {
      logger.error("Failed to process freemarker template: template={}, variables={}", template, variables, e);
      throw new BssException("处理 FreeMarker 模板失败: " + e.getMessage(), e);
    }
  }

  /**
   * 构造 FreeMarker 配置
   */
  private static Configuration buildFreemarkerConfiguration() {
    Configuration configuration = new Configuration(Configuration.VERSION_2_3_33);
    configuration.setDefaultEncoding(StandardCharsets.UTF_8.name());
    // 避免检查 locale 模板的开销
    configuration.setLocalizedLookup(false);
    // 避免格式化数字的显示
    configuration.setNumberFormat("#");
    return configuration;
  }

}
