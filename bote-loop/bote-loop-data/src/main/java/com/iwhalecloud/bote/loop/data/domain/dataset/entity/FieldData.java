package com.iwhalecloud.bote.loop.data.domain.dataset.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 字段数据实体
 * 迁移对应关系: Go语言entity.FieldData
 * - 功能: 存储字段数据信息
 * - 字段定义:
 * * Key: string - 字段键
 * * Name: string - 字段名称
 * * ContentType: ContentType - 内容类型
 * * Format: FieldDisplayFormat - 显示格式
 * * Content: string - 内容
 * * Attachments: []*ObjectStorage - 附件
 * * Parts: []*FieldData - 部分数据
 * <p>
 * Java实现说明:
 * - 对应Go的entity.FieldData结构体
 * - 使用Java类定义，包含字段数据字段
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * - 包含业务逻辑方法
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go json标签 -> Jackson注解
 * - Go切片 -> Java List
 * - Go方法 -> Java方法
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FieldData {

  @JsonProperty("key")
  private String key;

  @JsonProperty("name")
  private String name;

  @JsonProperty("content_type")
  private ContentType contentType;

  @JsonProperty("format")
  private FieldDisplayFormat format;

  @JsonProperty("content")
  private String content;

  @JsonProperty("attachments")
  private List<ObjectStorage> attachments;

  @JsonProperty("parts")
  private List<FieldData> parts;

  /**
   * 获取数据字节数
   * 迁移对应关系: Go语言entity.FieldData.DataBytes()
   * - 功能: 计算字段数据的字节数
   * - 返回: 字节数
   */
  @JsonIgnore
  public int getDataBytes() {
    int bytes = 0;
    if (content != null) {
      bytes += content.length();
    }
    if (attachments != null) {
      for (ObjectStorage att : attachments) {
        if (att.getName() != null) {
          bytes += att.getName().length();
        }
        if (att.getUri() != null) {
          bytes += att.getUri().length();
        }
      }
    }
    if (parts != null) {
      for (FieldData part : parts) {
        bytes += part.getDataBytes();
      }
    }
    return bytes;
  }

  /**
   * 获取数据字符数
   * 迁移对应关系: Go语言entity.FieldData.DataRunes()
   * - 功能: 计算字段数据的字符数
   * - 返回: 字符数
   */
  @JsonIgnore
  public int getDataRunes() {
    int runes = 0;
    if (content != null) {
      runes += content.length();
    }
    if (attachments != null) {
      for (ObjectStorage att : attachments) {
        if (att.getName() != null) {
          runes += att.getName().length();
        }
        if (att.getUri() != null) {
          runes += att.getUri().length();
        }
      }
    }
    if (parts != null) {
      for (FieldData part : parts) {
        runes += part.getDataRunes();
      }
    }
    return runes;
  }
}
