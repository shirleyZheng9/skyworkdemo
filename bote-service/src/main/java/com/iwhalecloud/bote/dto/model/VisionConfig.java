package com.iwhalecloud.bote.dto.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 大模型节点的视觉配置
 *
 * @author bianjp
 * @since 2025-02-08
 */
@Getter
@Setter
@ToString
public class VisionConfig {
  /** 是否启用 */
  private Boolean enabled;
  /** 图片地址或文件 ID(取值表达式), 常量值只能是一个文件，引用变量时可以是多个文件 */
  private String files;
}
