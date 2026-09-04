package com.iwhalecloud.bote.filter;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * @author zhang.yinglin
 * @since 2022/7/19
 */
public class EncryptionResponseWrapper extends HttpServletResponseWrapper {

  private ServletOutputStream filterOutput;

  private final ByteArrayOutputStream output;
  private final PrintWriter writer;

  @SuppressFBWarnings("DM_DEFAULT_ENCODING")
  public EncryptionResponseWrapper(HttpServletResponse response) {
    super(response);
    output = new ByteArrayOutputStream();
    writer = new PrintWriter(output);
  }

  @Override
  public ServletOutputStream getOutputStream() {
    if (filterOutput == null) {
      filterOutput = new ServletOutputStream() {
        @Override
        public void write(int b) {
          output.write(b);
        }

        @Override
        public boolean isReady() {
          return false;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
          // 不支持
        }
      };
    }

    return filterOutput;
  }

  @Override
  public PrintWriter getWriter() {
    return writer;
  }

  public String getResponseData() {
    writer.flush();
    return output.toString(StandardCharsets.UTF_8);
  }

}
