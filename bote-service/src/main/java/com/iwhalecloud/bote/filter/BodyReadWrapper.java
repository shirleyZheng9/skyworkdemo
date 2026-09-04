package com.iwhalecloud.bote.filter;


import org.springframework.util.StreamUtils;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * HttpServletRequest 对象包装类。
 * 参数签名校验需要读取body，解决request body只能读取一次的问题。
 * 不能应用于文件上传场景。
 *
 * @author zhangJun
 * @since 2022/3/23
 **/
public class BodyReadWrapper extends HttpServletRequestWrapper {

  /** 请求body */
  private byte[] requestBody;
  /** 是否已读 */
  private boolean isRead = false;


  public BodyReadWrapper(HttpServletRequest request) {
    super(request);
  }

  @Override
  public BufferedReader getReader() throws IOException {
    return new BufferedReader(new InputStreamReader(getInputStream(), super.getCharacterEncoding()));
  }

  @Override
  public ServletInputStream getInputStream() throws IOException {
    return getInStream();
  }

  private ServletInputStream getInStream() throws IOException {
    readBody();
    final ByteArrayInputStream inputStream = new ByteArrayInputStream(requestBody);
    return new ServletInputStream() {
      @Override
      public int read() {
        return inputStream.read();
      }

      @Override
      public boolean isFinished() {
        return false;
      }

      @Override
      public boolean isReady() {
        return false;
      }

      @Override
      public void setReadListener(ReadListener listener) {
        // 不支持
      }
    };
  }

  private synchronized void readBody() throws IOException {
    if (isRead) {
      return;
    }
    isRead = true;
    // 如果是上传文件，报文过大，会有问题
    requestBody = StreamUtils.copyToByteArray(getRequest().getInputStream());
  }
}
