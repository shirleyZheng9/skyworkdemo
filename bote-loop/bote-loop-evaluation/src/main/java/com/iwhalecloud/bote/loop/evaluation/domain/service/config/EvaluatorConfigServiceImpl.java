package com.iwhalecloud.bote.loop.evaluation.domain.service.config;

import com.iwhalecloud.bote.loop.evaluation.domain.entity.Function;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Tool;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ToolType;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 评估器配置服务实现
 * 迁移对应关系: Go语言conf.IConfiger实现
 * - 功能: 从Spring配置中获取评估器配置
 * - 数据源: application.yml/properties配置文件
 * <p>
 * Java实现说明:
 * - 对应Go的IConfiger接口实现
 * - 使用Spring ConfigurationProperties
 * - 支持配置热更新
 */
@Service
@RequiredArgsConstructor
public class EvaluatorConfigServiceImpl implements EvaluatorConfigService {
  private final EvaluatorConfigProperties configProperties;

  @Override
  public Map<String, Tool> getEvaluatorToolConf() {
    return configProperties.getToolConf().entrySet().stream()
      .collect(Collectors.toMap(
        Map.Entry::getKey,
        entry -> convertToolConfigToTool(entry.getValue())
      ));
  }

  @Override
  public Map<String, String> getEvaluatorToolMapping() {
    return new HashMap<>(configProperties.getToolMapping());
  }

  @Override
  public Map<String, String> getEvaluatorPromptSuffix() {
    return new HashMap<>(configProperties.getPromptSuffix());
  }

  @Override
  public Map<String, String> getEvaluatorPromptSuffixMapping() {
    return new HashMap<>(configProperties.getPromptSuffixMapping());
  }

  /**
   * 将配置中的ToolConfig转换为Tool实体
   * 迁移对应关系: Go语言evaluator.ConvertToolDTO2DO
   */
  private Tool convertToolConfigToTool(EvaluatorConfigProperties.ToolConfig toolConfig) {
    Function function = Function.builder()
      .name(toolConfig.getFunction().getName())
      .description(toolConfig.getFunction().getDescription())
      .parameters(toolConfig.getFunction().getParameters())
      .build();

    return Tool.builder()
      .type(ToolType.fromValue(Integer.parseInt(toolConfig.getType())))
      .function(function)
      .build();
  }
}
