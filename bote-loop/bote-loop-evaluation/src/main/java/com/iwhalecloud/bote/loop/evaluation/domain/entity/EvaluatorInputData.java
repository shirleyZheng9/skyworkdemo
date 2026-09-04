package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评估器输入数据实体
 * 迁移对应关系: Go语言EvaluatorInputData
 * - 功能: 评估器输入数据结构
 * - 字段: historyMessages, inputFields
 * <p>
 * Java实现说明:
 * - 对应Go的EvaluatorInputData结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go []*Message -> Java List<Message>
 * - Go map[string]*Content -> Java Map<String, Content>
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluatorInputData {
  @JsonProperty("history_messages")
  private List<Message> historyMessages;

  @JsonProperty("input_fields")
  private Map<String, Content> inputFields;
}
