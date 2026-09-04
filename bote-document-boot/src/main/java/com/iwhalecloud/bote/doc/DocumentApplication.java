package com.iwhalecloud.bote.doc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 文档中心服务启动类
 */
@SpringBootApplication(proxyBeanMethods = false)
@SuppressWarnings("PMD.UseUtilityClass")
public class DocumentApplication {

  public static void main(String[] args) {
    SpringApplication.run(DocumentApplication.class, args);
  }
}
