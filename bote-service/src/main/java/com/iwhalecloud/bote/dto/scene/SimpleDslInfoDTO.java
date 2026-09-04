package com.iwhalecloud.bote.dto.scene;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 简单 DSL 信息（统一复杂场景、工作流的 DSL 表示）
 *
 * @author bianjp
 * @since 2024-12-10
 */
@Getter
@Setter
@ToString
public class SimpleDslInfoDTO {
  /** ID */
  private Long id;
  /** 编码 */
  private String code;
  /** 名称 */
  private String name;
  /** 是否是对话流 */
  private Boolean chatflow;
  /** DSL JSON 字符串 */
  private String dslJson;
  /** 流程步骤 JSON 字符串 */
  private String flowStepJson;
}
