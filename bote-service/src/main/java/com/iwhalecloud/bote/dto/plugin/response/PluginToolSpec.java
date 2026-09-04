package com.iwhalecloud.bote.dto.plugin.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 插件工具(含参数)
 *
 * @author chen.linfa
 * @since 2025-12-13
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
public class PluginToolSpec {
  /** 工具名称(英文编码) */
  private String name;
  /** 工具标题 */
  private String title;
  /** 描述 */
  private String description;
  /** 面向大模型的描述 */
  private String llmDescription;
  /** 插件入参 */
  private ToolInputSpec input;
  /** 插件出参 */
  private ToolOutputSpec output;


}
