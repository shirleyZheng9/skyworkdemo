package com.iwhalecloud.bote.loop.prompt.domain.repo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Prompt Key版本对
 * 迁移对应关系: Go语言service.PromptKeyVersionPair
 * - 功能: Prompt Key和版本的配对
 * - 字段:
 * * promptKey - Prompt键
 * * version - 版本
 * <p>
 * Java实现说明:
 * - 对应Go的service.PromptKeyVersionPair结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * - 实现equals和hashCode方法用于Map键
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go string -> Java String
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromptKeyVersionPair {

  private String promptKey;

  private String version;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PromptKeyVersionPair that = (PromptKeyVersionPair) o;
    return java.util.Objects.equals(promptKey, that.promptKey) &&
      java.util.Objects.equals(version, that.version);
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(promptKey, version);
  }
}
