package com.iwhalecloud.bote.adapter.juzhi2;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

/**
 * 二级聚智模拟接口
 *
 * @author tingyun.wang
 * @since 2025-06-27
 */
@RestController
@RequestMapping("openapi/flames")
public class Juzhi2MockController {

  private static final Logger logger = LoggerFactory.getLogger(Juzhi2MockController.class);

  /** 文件上传接口响应 */
  private final ClassPathResource fileUploadResponseResource = new ClassPathResource("mock-response/juzhi1/file.json");

  /**
   * 文件上传接口
   */
  @PostMapping(path = "file/v2/upload", produces = MediaType.APPLICATION_JSON_VALUE)
  @SuppressWarnings("PMD.GuardLogStatement")
  public void fileUpload(@RequestBody String requestBody, HttpServletResponse response) throws IOException {
    logger.debug("Received juzhi2 file request: requestBody size = {}", requestBody.length());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    try (InputStream inputStream = fileUploadResponseResource.getInputStream()) {
      IOUtils.copy(inputStream, response.getOutputStream());
    }
  }

}
