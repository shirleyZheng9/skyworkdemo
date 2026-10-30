package com.iwhalecloud.bote.common.consts;

import com.google.common.collect.ImmutableList;
import java.util.List;

/**
 * 大模型相关常量
 *
 * @author bianjp
 * @since 2024-12-23
 */
public final class ModelConsts {
  private ModelConsts() {
  }

  /** 模型类型: 大语言 */
  public static final String MODEL_TYPE_LLM = "llm";
  /** 模型类型: 文本嵌入 */
  public static final String MODEL_TYPE_EMBEDDING = "embedding";
  /** 模型类型列表 */
  public static final List<String> MODEL_TYPES = ImmutableList.of(MODEL_TYPE_LLM, MODEL_TYPE_EMBEDDING);

  /** 模型协议类型: openai */
  public static final String MODEL_PROTOCOL_OPENAI = "openai";

  /** 默认模型(虚拟的模型 ID, 表示使用机器人/租户/平台配置的默认模型) */
  public static final Long DEFAULT_MODEL = -1L;

  /**
   * 智能体绑定的大模型已删除时的提示
   * <p>
   * 保存智能体（BotSceneManageServiceImpl#validateModel）和智能体对话（AbstractSceneRunner#resolveModelId）共用，
   * 保证两处提示一致。业务约定：大模型删除后，绑定该模型的智能体不允许继续对话，需要更换大模型。
   * 注意：大模型的"启用/停用"（is_enabled）表示是否为空间/平台 AI 助手的默认模型，同一空间只能启用一个，
   * 并不表示模型不可用，因此"停用"的模型不做拦截。
   */
  public static final String MODEL_DELETED_MSG = "当前大模型已删除，请更换大模型";

  /** json 内容前缀 */
  public static final String JSON_PREFIX = "```json\n";
  /** json 内容后缀 */
  public static final String JSON_SUFFIX = "\n```";
  /** 代码块前缀 */
  public static final String CODE_PREFIX = "```\n";
  /** 代码块反引号 */
  public static final String BACKTICK = "```";

  /** 对话来源：智能体 */
  public static final String SOURCE_AGENT = "agent";
  /** 对话来源：调试 */
  public static final String SOURCE_TEST = "test";
  /** 对话来源：BoteClaw */
  public static final String SOURCE_BOTECLAW = "boteclaw";
  /** 对话来源：大模型对话 */
  public static final String SOURCE_LLM = "llm";
}
