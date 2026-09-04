package com.iwhalecloud.bote.loop.evaluation.infra.pdf.module;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ExperimentBaseInfo {
  /** 评测集 */
  private String evalSet;
  /** 评测对象类型 */
  private String evalTargetType;
  /** 评测对象 */
  private String evalTarget;
  /** 评估器 */
  private List<String> evaluatorNameVersions;
  /** 创建人 */
  private String createdBy;
  /** 创建时间 */
  private String createdAt;
  /** 结束事件 */
  private String endDate;
  /** 描述 */
  private String description;
}
