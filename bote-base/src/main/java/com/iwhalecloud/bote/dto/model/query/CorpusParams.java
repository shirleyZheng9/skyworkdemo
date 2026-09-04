package com.iwhalecloud.bote.dto.model.query;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 微调语料参数
 *
 * @author chen.linfa
 * @since 2025-04-18
 */
@Getter
@Setter
@ToString
public class CorpusParams {
  /** 语料 ID */
  private Long corpusId;

  /** 语料名称 */
  private String corpusName;

  /** 问题列名 */
  private String question;

  /** 答案列名 */
  private String answer;
}
