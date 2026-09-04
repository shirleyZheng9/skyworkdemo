package com.iwhalecloud.bote.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.util.HashMap;
import java.util.Map;
import lombok.Setter;

/**
 * 表单请求 HttpServletRequest 包装类
 * <p>用于处理请求content-type 为 multipart/form-data 或者 application/x-www-form-urlencoded</p>
 *
 * @author zhang.jiaxin
 * @since 2024-08-15
 */
@Setter
public class EncryptionFormRequestWrapper extends HttpServletRequestWrapper {

  private Map<String, String[]> paramMap = new HashMap<>();

  public EncryptionFormRequestWrapper(HttpServletRequest request) {
    super(request);
  }

  @Override
  public String getParameter(String name) {
    String[] values = this.paramMap.get(name);
    if (values != null && values.length > 0) {
      return values[0];
    }
    return super.getParameter(name);
  }

  @Override
  public String[] getParameterValues(String name) {
    if (paramMap.containsKey(name)) {
      return this.paramMap.get(name);
    }
    return super.getParameterValues(name);
  }

  @Override
  public Map<String, String[]> getParameterMap() {
    return this.paramMap;
  }

}
