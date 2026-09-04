package com.iwhalecloud.bote.doc.module.knowledge.semantic.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 索引文档DTO
 *
 * @author qian.sisheng
 * @since 2026-04-07
 */
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class IndexDocDTO {
  /** 消息ID */
  private String msgId;
  /** 原始文本 */
  private String question;
  /** 标准问题 */
  private String standardQuestion;
  /** 向量 */
  private float[] vector;
  /** 租户ID */
  private Long tenantId;
  /** 近邻匹配分数（[0,1]） */
  private Float matchScore;

  /**
   * 转换为文档源
   */
  public Map<String, Object> toSource() {
    Map<String, Object> source = new HashMap<>(8);
    source.put("tenantId", tenantId);
    source.put("msgId", msgId);
    source.put("question", question);
    source.put("standardQuestion", standardQuestion);
    source.put("matchScore", matchScore);
    source.put("question_vector", floatArrayToList(vector));
    return source;
  }

  /**
   * float 数组转 List
   */
  private List<Float> floatArrayToList(float[] array) {
    List<Float> list = new ArrayList<>(array.length);
    for (float v : array) {
      list.add(v);
    }
    return list;
  }
}
