package com.iwhalecloud.bote.adapter.juzhi2;

import java.io.IOException;
import java.io.InputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 一级聚智模拟接口
 *
 * @author bianjp
 * @since 2025-05-20
 */
@RestController
@RequestMapping("juzhi1")
public class Juzhi1MockController {
  private static final Logger logger = LoggerFactory.getLogger(Juzhi1MockController.class);
  /** 大模型接口响应 */
  private final ClassPathResource llmResponseResource = new ClassPathResource("mock-response/juzhi1/llm.json");

  /**
   * 大模型接口
   */
  @PostMapping(path = "llm", produces = MediaType.APPLICATION_JSON_VALUE)
  public void llm(@RequestBody String requestBody, HttpServletResponse response) throws IOException {
    logger.debug("Received juzhi1 llm request: {}", requestBody);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    if (requestBody.contains("智脑对话记录保存")) {
      response.getWriter().write("{}");
      return;
    }
    try (InputStream inputStream = llmResponseResource.getInputStream()) {
      IOUtils.copy(inputStream, response.getOutputStream());
    }
  }
}
