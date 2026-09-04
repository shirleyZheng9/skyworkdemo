package com.iwhalecloud.bote.loop.client.prompt.manage.dto;

import lombok.Getter;

/**
 * 列表Prompt排序类型枚举
 * 对应Thrift: ListPromptOrderBy
 */
@Getter
public enum ListPromptOrderBy {
  COMMITTED_AT("committed_at"),
  UPDATED_AT("updated_at"),
  CREATED_AT("created_at");

  private final String value;

  ListPromptOrderBy(String value) {
    this.value = value;
  }

  public static ListPromptOrderBy fromValue(String value) {
    for (ListPromptOrderBy orderBy : values()) {
      if (orderBy.value.equals(value)) {
        return orderBy;
      }
    }
    throw new IllegalArgumentException("Unknown ListPromptOrderBy: " + value);
  }
}
