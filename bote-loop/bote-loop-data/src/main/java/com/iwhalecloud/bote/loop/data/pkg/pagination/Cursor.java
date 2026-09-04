package com.iwhalecloud.bote.loop.data.pkg.pagination;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * 游标DTO
 * 迁移对应关系: Go语言pagination.Cursor
 * - 功能: 存储分页游标信息
 * - 字段定义: 时间和ID值
 * <p>
 * Java实现说明:
 * - 对应Go的pagination.Cursor结构体
 * - 使用Java类定义，包含游标字段
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go json标签 -> Jackson注解
 * - Go指针类型 -> Java对象引用
 * - Go time.Time -> Java LocalDateTime
 */
public class Cursor {

  @JsonProperty("time_value")
  private LocalDateTime timeValue;

  @JsonProperty("id_value")
  private Long idValue;

  public Cursor() {
  }

  public Cursor(LocalDateTime timeValue, Long idValue) {
    this.timeValue = timeValue;
    this.idValue = idValue;
  }

  public LocalDateTime getTimeValue() {
    return timeValue;
  }

  public void setTimeValue(LocalDateTime timeValue) {
    this.timeValue = timeValue;
  }

  public Long getIdValue() {
    return idValue;
  }

  public void setIdValue(Long idValue) {
    this.idValue = idValue;
  }

  /**
   * 编码游标
   * 迁移对应关系: Go语言pagination.Cursor.Encode
   * - 功能: 将游标编码为字符串
   * - 返回: 编码后的游标字符串
   * - 用途: 生成下一页游标
   */
  public String encode() {
    // 这里需要实现具体的编码逻辑
    // 可以使用Base64编码或其他方式
    return String.format("%s:%s",
      timeValue != null ? timeValue.toString() : "",
      idValue != null ? idValue.toString() : "");
  }

  /**
   * 解码游标
   * 迁移对应关系: Go语言pagination.decodeCursor
   * - 功能: 将字符串解码为游标
   * - 参数: cursor - 游标字符串
   * - 返回: 游标对象
   * - 用途: 解析上一页游标
   */
  public static Cursor decode(String cursor) {
    if (cursor == null || cursor.isEmpty()) {
      return new Cursor();
    }

    String[] parts = cursor.split(":");
    if (parts.length != 2) {
      return new Cursor();
    }

    LocalDateTime timeValue = null;
    Long idValue = null;

    if (!parts[0].isEmpty()) {
      try {
        timeValue = LocalDateTime.parse(parts[0]);
      }
      catch (Exception e) {
        // 忽略解析错误
      }
    }

    if (!parts[1].isEmpty()) {
      try {
        idValue = Long.parseLong(parts[1]);
      }
      catch (Exception e) {
        // 忽略解析错误
      }
    }

    return new Cursor(timeValue, idValue);
  }
}
