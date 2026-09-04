package com.iwhalecloud.bote.llm.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 请求头
 *
 * @author bianjp
 * @since 2025-03-14
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
public class HeaderItem {
  /** 请求头名称 */
  private String name;
  /** 请求头值 */
  private String value;
  /** 描述 */
  private String description;

  public HeaderItem() {
  }

  public HeaderItem(String name, String value) {
    this.name = name;
    this.value = value;
  }

  public HeaderItem(String name, String value, String description) {
    this.name = name;
    this.value = value;
    this.description = description;
  }
}
