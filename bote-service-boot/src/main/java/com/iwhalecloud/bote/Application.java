package com.iwhalecloud.bote;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 应用启动类
 *
 * @author bianjp
 * @since 2024-07-11
 */
@SpringBootApplication(proxyBeanMethods = false)
@SuppressWarnings("PMD.UseUtilityClass")
public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

}
