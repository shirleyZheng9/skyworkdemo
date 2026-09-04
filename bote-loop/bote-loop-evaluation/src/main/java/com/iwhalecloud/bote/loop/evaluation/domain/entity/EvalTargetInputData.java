package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评估目标输入数据实体
 * 迁移对应关系: Go语言EvalTargetInputData
 * - 功能: 评估目标输入数据结构
 * - 字段: historyMessages, inputFields, ext
 * <p>
 * Java实现说明:
 * - 对应Go的EvalTargetInputData结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * - 实现ValidateInputSchema方法
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go []*Message -> Java List<Message>
 * - Go map[string]*Content -> Java Map<String, Content>
 * - Go map[string]string -> Java Map<String, String>
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvalTargetInputData {
  @JsonProperty("history_messages")
  private List<Message> historyMessages;

  @JsonProperty("input_fields")
  private Map<String, Content> inputFields;

  @JsonProperty("ext")
  private Map<String, String> ext;

  /**
   * 验证输入模式
   * 迁移对应关系: Go语言EvalTargetInputData.ValidateInputSchema()
   */
  public void validateInputSchema(List<ArgsSchema> inputSchema) {
    // 实现验证逻辑
    // 这里需要根据具体的业务逻辑来实现
  }
}
