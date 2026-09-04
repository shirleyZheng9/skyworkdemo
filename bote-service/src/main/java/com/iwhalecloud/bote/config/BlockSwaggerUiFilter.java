package com.iwhalecloud.bote.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springdoc.core.utils.Constants;
import org.springdoc.webmvc.ui.SwaggerWelcomeWebMvc;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.web.servlet.FilterRegistration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 屏蔽 Swagger UI 过滤器
 *
 * <p>关闭 Swagger 文档时，屏蔽 Swagger UI 页面，以避免安全扫描当作漏洞</p>
 *
 * @author bianjp
 * @since 2025-12-10
 */
@Component
@ConditionalOnClass(SwaggerWelcomeWebMvc.class)
@ConditionalOnBooleanProperty(name = Constants.SPRINGDOC_ENABLED, havingValue = false)
@FilterRegistration(urlPatterns = {"/doc.html", "/webjars/*"}) // 目前只有 Swagger 文档有 webjars 静态资源
public class BlockSwaggerUiFilter extends OncePerRequestFilter {
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException {
    // 直接返回 404. 可参考 NoResourceFoundException
    response.setStatus(HttpStatus.NOT_FOUND.value());
    response.setContentType(MediaType.TEXT_PLAIN_VALUE);
    response.getOutputStream().write(HttpStatus.NOT_FOUND.getReasonPhrase().getBytes(StandardCharsets.UTF_8));
  }
}
