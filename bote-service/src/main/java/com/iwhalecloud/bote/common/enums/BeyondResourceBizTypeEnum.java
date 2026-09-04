package com.iwhalecloud.bote.common.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;

/**
 * 百应资源业务类型枚举
 *
 * @author lizuyin
 * @since 2025-07-21
 */
@Getter
@RequiredArgsConstructor
public enum BeyondResourceBizTypeEnum {
  /** 智能体 */
  @JsonProperty("AGENT")
  AGENT("AGENT", "智能体"),
  /** MCP服务 */
  @JsonProperty("MCP")
  MCP("MCP", "MCP服务"),
  /** 工作流 */
  @JsonProperty("TOOL")
  TOOL("TOOL", "工作流"),
  /** 数字员工 */
  @JsonProperty("BOT")
  BOT("BOT", "数字员工");
  /** 资源业务类型编码 */
  private final String code;
  /** 资源业务类型描述 */
  private final String desc;
  /**
   * 根据编码获取枚举值
   */
  @Nullable
  public static BeyondResourceBizTypeEnum getByCode(@Nullable String code) {
    if (code == null) {
      return null;
    }
    for (BeyondResourceBizTypeEnum typeEnum : values()) {
      if (typeEnum.getCode().equals(code)) {
        return typeEnum;
      }
    }
    return null;
  }
}
