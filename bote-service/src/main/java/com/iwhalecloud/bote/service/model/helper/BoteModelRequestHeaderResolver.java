package com.iwhalecloud.bote.service.model.helper;

import com.iwhalecloud.bote.common.util.SceneParamUtil;
import com.iwhalecloud.bote.common.util.TemplateUtil;
import com.iwhalecloud.bote.llm.client.ext.ModelRequestHeaderResolver;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

/**
 * 模型请求头解析器
 *
 * @author bianjp
 * @since 2025-10-23
 */
@Service
public class BoteModelRequestHeaderResolver implements ModelRequestHeaderResolver {

  @Override
  public String resolve(String value) {
    return TemplateUtil.resolveTemplate(value, this::resolveTemplateParam);
  }

  /**
   * 解析模板字符串中的参数
   *
   * @param expression 参数表达式
   * @return 参数的值
   */
  @Nullable
  @SuppressWarnings("PMD.UnusedPrivateMethod")
  private Object resolveTemplateParam(String expression) {
    // 只支持系统变量、登录信息，不支持的原样返回
    if (!expression.startsWith("system.") && !expression.startsWith("session.")) {
      return "${" + expression + "}";
    }
    return SceneParamUtil.getParamValue("$." + expression);
  }

}
