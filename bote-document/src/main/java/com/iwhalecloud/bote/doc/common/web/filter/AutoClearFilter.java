package com.iwhalecloud.bote.doc.common.web.filter;

import com.iwhalecloud.bote.doc.common.context.AutoClear;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.web.filter.OncePerRequestFilter;


/**
 * 过滤器用于清空线程参数
 *
 * @author Aiqing
 * @since 2025/9/15
 */
public class AutoClearFilter extends OncePerRequestFilter {

  private static final Logger logger = LoggerFactory.getLogger(AutoClearFilter.class);

  private final List<AutoClear> autoClears = new ArrayList<>();

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {
    try {
      filterChain.doFilter(request, response);
    }
    finally {
      autoClears.forEach(AutoClear::clear);
    }
  }

  /**
   * 容器加载完成之后调用
   * 因一些特殊的bean，可能是在当前bean加载完之后才被注入到容器的，比如带有{@link @Primary}的bean
   *
   * @param event 事件
   */
  @EventListener
  public void init(ApplicationReadyEvent event) {
    ConfigurableApplicationContext applicationContext = event.getApplicationContext(); //NOPMD - suppressed CloseResource - 不能关闭
    Map<String, AutoClear> autoClearMap = applicationContext.getBeansOfType(AutoClear.class);
    if (MapUtils.isNotEmpty(autoClearMap)) {
      if (logger.isDebugEnabled()) {
        autoClearMap.forEach((key, value) -> {
          logger.debug("register autoClear: {}", value.getClass().getName());
        });
      }
      autoClears.addAll(autoClearMap.values());
    }
  }
}
