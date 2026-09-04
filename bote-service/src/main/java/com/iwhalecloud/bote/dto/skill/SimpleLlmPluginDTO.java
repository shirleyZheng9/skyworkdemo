package com.iwhalecloud.bote.dto.skill;

import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

/**
 * 简单大模型插件
 *
 * @author bianjp
 * @since 2024-12-18
 */
@Getter
@Setter
@ToString
public class SimpleLlmPluginDTO {
  /** 插件 ID */
  private Long pluginId;
  /** 插件名称 */
  private String pluginName;
  /** 插件编码 */
  private String pluginCode;
  /** 大模型 ID */
  private Long modelId;
  /** 提示词 ID */
  private Long promptId;
  /** 提示词内容 */
  private String promptContent;
  /** 前置脚本内容 */
  private String preScript;
  /** 后置脚本内容 */
  private String postScript;
  /** 入参 */
  private ParameterSpec request;

  /**
   * 转换插件对象
   */
  public static SimpleLlmPluginDTO from(SkillPluginDTO plugin) {
    SimpleLlmPluginDTO dto = new SimpleLlmPluginDTO();
    dto.pluginId = plugin.getApiId();
    dto.pluginName = plugin.getApiName();
    dto.pluginCode = plugin.getApiCode();
    dto.modelId = plugin.getModelId();
    dto.promptId = plugin.getPromptId();
    dto.promptContent = plugin.getPromptContent();
    dto.preScript = StringUtils.trimToNull(plugin.getPreScript());
    dto.postScript = StringUtils.trimToNull(plugin.getPostScript());
    if (StringUtils.isNotEmpty(plugin.getReqJson())) {
      dto.request = JsonUtil.parseJsonRequired(plugin.getReqJson(), ParameterSpec.class);
    }
    return dto;
  }
}
