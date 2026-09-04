package com.iwhalecloud.bote.loop.client.evaluation.domain.eval_target;

/**
 * 模型平台枚举
 */
public enum ModelPlatformDTO {
  UNKNOWN(0, "Unknown"),
  GPT_OPEN_API(1, "GPTOpenAPI"),
  MAAS(2, "MAAS");

  private final int value;
  private final String name;

  ModelPlatformDTO(int value, String name) {
    this.value = value;
    this.name = name;
  }

  public int getValue() {
    return value;
  }

  public String getName() {
    return name;
  }
}
