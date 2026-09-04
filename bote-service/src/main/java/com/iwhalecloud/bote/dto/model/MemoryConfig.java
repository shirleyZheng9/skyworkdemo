package com.iwhalecloud.bote.dto.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 记忆配置
 *
 * @author bianjp
 * @since 2024-09-04
 */
@Getter
@Setter
@ToString
public class MemoryConfig {
  /** 是否开启记忆。仅对话流类型的流程支持开启 */
  private Boolean enabled;
  /** 是否开启记忆窗口 */
  private Boolean windowEnabled;
  /** 记忆窗口大小 */
  private Integer windowSize;
}
