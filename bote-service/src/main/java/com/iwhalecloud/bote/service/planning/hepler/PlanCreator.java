package com.iwhalecloud.bote.service.planning.hepler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableMap;
import com.iwhalecloud.bote.common.util.FreemarkerUtil;
import com.iwhalecloud.bote.dto.planning.SimplePlanDTO;
import com.iwhalecloud.bote.dto.planning.SimplePlanDTO.SimplePlanStepDTO;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * 负责生成编排的 DSL
 *
 * @author chen.linfa
 * @since 2025-07-01
 */
@Component
@RequiredArgsConstructor
public class PlanCreator {

  /** DSL 模板 */
  private static final ClassPathResource DSL_TEMPLATE = new ClassPathResource("planning/template.json.ftl");

  /**
   * 生成 DSL
   */
  public String generateDsl(SimplePlanDTO plan) {
    return buildDsl(plan.getSteps());
  }

  private String buildDsl(List<SimplePlanStepDTO> steps) {
    String template;
    try (InputStream templateStream = DSL_TEMPLATE.getInputStream()) {
      template = IOUtils.toString(templateStream, StandardCharsets.UTF_8);
    }
    catch (IOException e) {
      throw new BssException("读取 DSL 模板失败: " + e.getMessage(), e);
    }
    // 处理模板
    String content = FreemarkerUtil.process(template, ImmutableMap.of("steps", steps));
    Map<String, Object> dsl = JsonUtil.parseJson(content, new TypeReference<Map<String, Object>>() {
    });
    return MapUtils.isNotEmpty(dsl) ? JsonUtil.toJsonString(dsl) : content;
  }
}
