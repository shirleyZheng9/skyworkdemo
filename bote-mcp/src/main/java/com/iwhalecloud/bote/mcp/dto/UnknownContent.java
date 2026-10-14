package com.iwhalecloud.bote.mcp.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import lombok.ToString;

/**
 * 未知内容类型，兜底解析并保留原始字段
 *
 * <p>MCP 协议演进会引入新的内容类型，未识别的类型降级为本类，
 * 避免反序列化失败导致工具调用整体失败。</p>
 *
 * @author zhusenliang
 * @since 2026-09-08
 */
@Getter
@ToString
@JsonInclude(Include.NON_NULL)
public class UnknownContent extends Content {
  /** 原始字段（包含 type） */
  @JsonIgnore
  private final Map<String, Object> fields = new LinkedHashMap<>();

  public UnknownContent() {
    super(null);
  }

  @JsonAnySetter
  public void putField(String name, Object value) {
    this.fields.put(name, value);
  }

  @JsonAnyGetter
  public Map<String, Object> getFields() {
    return this.fields;
  }
}
