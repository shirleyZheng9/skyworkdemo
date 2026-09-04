package com.iwhalecloud.bote.loop.prompt.domain.entity;

import com.iwhalecloud.bote.common.util.TemplateUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Prompt模板实体
 * 迁移对应关系: Go语言entity.PromptTemplate
 * - 功能: 存储Prompt的消息模板和变量定义
 * - 字段定义:
 * * TemplateType: TemplateType - 模板类型
 * * Messages: []*Message - 消息列表
 * * VariableDefs: []*VariableDef - 变量定义列表
 * <p>
 * Java实现说明:
 * - 对应Go的entity.PromptTemplate结构体
 * - 使用Java类定义，包含模板相关字段
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * - 实现消息格式化和模板消息获取方法
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go json标签 -> Jackson注解
 * - Go切片类型 -> Java List
 * - Go枚举类型 -> Java枚举
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromptTemplate {
  /**
   * 模板类型
   * 迁移对应关系: Go语言entity.PromptTemplate.TemplateType (TemplateType)
   * - 功能: 模板的类型标识
   * - 类型: Go的枚举类型对应Java的枚举
   * - 用途: 区分不同的模板类型
   */
  private TemplateType templateType;
  /**
   * 消息列表
   * 迁移对应关系: Go语言entity.PromptTemplate.Messages ([]*Message)
   * - 功能: 模板中的消息列表
   * - 类型: Go的切片类型对应Java的List
   * - 用途: 存储模板消息内容
   */
  private List<Message> messages;
  /**
   * 变量定义列表
   * 迁移对应关系: Go语言entity.PromptTemplate.VariableDefs ([]*VariableDef)
   * - 功能: 模板中使用的变量定义
   * - 类型: Go的切片类型对应Java的List
   * - 用途: 定义模板变量的类型和描述
   */
  private List<VariableDef> variableDefs;

  /**
   * 格式化消息
   * 迁移对应关系: Go语言entity.PromptTemplate.formatMessages
   * - 功能: 根据变量值格式化消息
   * - 参数: 消息列表、变量值列表
   * - 返回: 格式化后的消息列表
   */
  public List<Message> formatMessages(List<Message> messages, List<VariableVal> variableVals) {
    List<Message> messagesToFormat = getTemplateMessages(messages);
    Map<String, VariableDef> defMap = buildVariableDefMap();
    Map<String, VariableVal> valMap = buildVariableValMap(variableVals);
    return processMessages(messagesToFormat, defMap, valMap);
  }

  private Map<String, VariableDef> buildVariableDefMap() {
    Map<String, VariableDef> defMap = new HashMap<>();
    if (this.variableDefs != null) {
      for (VariableDef variableDef : this.variableDefs) {
        if (variableDef != null) {
          defMap.put(variableDef.getKey(), variableDef);
        }
      }
    }
    return defMap;
  }

  private Map<String, VariableVal> buildVariableValMap(List<VariableVal> variableVals) {
    Map<String, VariableVal> valMap = new HashMap<>();
    if (variableVals != null) {
      for (VariableVal variableVal : variableVals) {
        if (variableVal != null) {
          valMap.put(variableVal.getKey(), variableVal);
        }
      }
    }
    return valMap;
  }

  private List<Message> processMessages(List<Message> messagesToFormat,
                                        Map<String, VariableDef> defMap,
                                        Map<String, VariableVal> valMap) {
    List<Message> formattedMessages = new java.util.ArrayList<>();
    for (Message message : messagesToFormat) {
      if (message == null) {
        continue;
      }
      if (message.getRole() == Role.PLACEHOLDER) {
        processPlaceholderMessage(message, valMap, formattedMessages);
      }
      else {
        processRegularMessage(message, defMap, valMap, formattedMessages);
      }
    }
    return formattedMessages;
  }

  private void processPlaceholderMessage(Message message, Map<String, VariableVal> valMap,
                                         List<Message> formattedMessages) {
    String content = message.getContent();
    if (content != null && valMap.containsKey(content)) {
      VariableVal placeholderVal = valMap.get(content);
      if (placeholderVal != null && placeholderVal.getPlaceholderMessages() != null) {
        addValidPlaceholderMessages(placeholderVal.getPlaceholderMessages(), formattedMessages);
      }
    }
  }

  private void addValidPlaceholderMessages(List<Message> placeholderMessages, List<Message> formattedMessages) {
    for (Message placeholderMessage : placeholderMessages) {
      if (placeholderMessage != null && isValidRole(placeholderMessage.getRole())) {
        formattedMessages.add(placeholderMessage);
      }
    }
  }

  private boolean isValidRole(Role role) {
    return role == Role.SYSTEM || role == Role.USER || role == Role.ASSISTANT || role == Role.TOOL;
  }

  private void processRegularMessage(Message message, Map<String, VariableDef> defMap,
                                     Map<String, VariableVal> valMap, List<Message> formattedMessages) {
    formatMessageContent(message, defMap, valMap);
    formatMessageParts(message, defMap, valMap);
    formattedMessages.add(message);
  }

  private void formatMessageContent(Message message, Map<String, VariableDef> defMap,
                                    Map<String, VariableVal> valMap) {
    if (message.getContent() != null && !message.getContent().isEmpty()) {
      String formattedStr = formatText(this.templateType, message.getContent(), defMap, valMap);
      message.setContent(formattedStr);
    }
  }

  private void formatMessageParts(Message message, Map<String, VariableDef> defMap,
                                  Map<String, VariableVal> valMap) {
    if (message.getParts() != null) {
      for (ContentPart part : message.getParts()) {
        if (part.getType() == ContentType.TEXT && part.getText() != null && !part.getText().isEmpty()) {
          String formattedStr = formatText(this.templateType, part.getText(), defMap, valMap);
          part.setText(formattedStr);
        }
      }
    }
  }

  /**
   * 获取模板消息
   * 迁移对应关系: Go语言entity.PromptTemplate.getTemplateMessages
   * - 功能: 获取模板消息和传入消息的组合
   * - 参数: 消息列表
   * - 返回: 组合后的消息列表
   */
  public List<Message> getTemplateMessages(List<Message> messages) {
    List<Message> messagesToFormat = new java.util.ArrayList<>();
    if (this.messages != null) {
      messagesToFormat.addAll(this.messages);
    }
    if (messages != null) {
      messagesToFormat.addAll(messages);
    }
    return messagesToFormat;
  }

  /**
   * 格式化文本
   * 迁移对应关系: Go语言formatText函数
   * - 功能: 根据模板类型格式化文本
   * - 参数: 模板类型、模板字符串、变量定义映射、变量值映射
   * - 返回: 格式化后的字符串
   */
  private String formatText(TemplateType templateType, String templateStr,
                            Map<String, VariableDef> defMap,
                            Map<String, VariableVal> valMap) {
    if (Objects.requireNonNull(templateType) == TemplateType.NORMAL) {
      return TemplateUtil.resolveTemplate(templateStr, key -> {
        if (defMap.containsKey(key)) {
          VariableVal val = valMap.get(key);
          return (val != null && val.getValue() != null) ? val.getValue() : "";
        }
        return "";
      });
    }
    throw new IllegalArgumentException("Unknown template type: " + templateType);
  }
}
