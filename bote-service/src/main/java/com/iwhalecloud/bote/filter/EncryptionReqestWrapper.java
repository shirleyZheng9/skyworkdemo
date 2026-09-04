package com.iwhalecloud.bote.filter;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import lombok.Setter;
import org.apache.commons.io.IOUtils;

/**
 * @author zhang.yinglin
 * @since 2022/7/19
 */
public final class EncryptionReqestWrapper extends HttpServletRequestWrapper {

  private byte[] requestBody;
  @Setter
  private Map<String, String[]> paramMap = new HashMap<>();

  public EncryptionReqestWrapper(HttpServletRequest request) {
    super(request);
    try {
      requestBody = IOUtils.toByteArray(request.getInputStream());
    }
    catch (IOException e) {
      throw new SecurityException(e);
    }
  }

  @Override
  public ServletInputStream getInputStream() {
    final ByteArrayInputStream bais = new ByteArrayInputStream(requestBody);
    return new ServletInputStream() {
      @Override
      public int read() {
        return bais.read();
      }

      @Override
      public boolean isFinished() {
        return false;
      }

      @Override
      public boolean isReady() {
        return true;
      }

      @Override
      public void setReadListener(ReadListener listener) {
        // 不支持
      }
    };
  }

  public String getRequestData() {
    return new String(requestBody, StandardCharsets.UTF_8);
  }

  public void setRequestData(String requestData) {
    this.requestBody = requestData.getBytes(StandardCharsets.UTF_8);
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
