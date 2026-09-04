package com.iwhalecloud.bote.loop.client.prompt.domain.prompt;

/**
 * Prompt相关常量
 * 迁移对应关系: Thrift常量定义
 */
public class PromptConstants {

  // TemplateType常量
  public static final String TEMPLATE_TYPE_NORMAL = "normal";

  // ToolType常量
  public static final String TOOL_TYPE_FUNCTION = "function";

  // ToolChoiceType常量
  public static final String TOOL_CHOICE_TYPE_NONE = "none";
  public static final String TOOL_CHOICE_TYPE_AUTO = "auto";

  // Role常量
  public static final String ROLE_SYSTEM = "system";
  public static final String ROLE_USER = "user";
  public static final String ROLE_ASSISTANT = "assistant";
  public static final String ROLE_TOOL = "tool";
  public static final String ROLE_PLACEHOLDER = "placeholder";

  // ContentType常量
  public static final String CONTENT_TYPE_TEXT = "text";
  public static final String CONTENT_TYPE_IMAGE_URL = "image_url";

  // VariableType常量
  public static final String VARIABLE_TYPE_STRING = "string";
  public static final String VARIABLE_TYPE_PLACEHOLDER = "placeholder";

  // Scenario常量
  public static final String SCENARIO_DEFAULT = "default";
  public static final String SCENARIO_EVAL_TARGET = "eval_target";
}
