package com.iwhalecloud.bote.sandbox.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 搜索文件结果
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
public class SandboxFileSearchResult {
  /** 是否成功 */
  private boolean success;
  /** 错误信息 */
  private String errorMessage;
  /** 文件列表 */
  private List<String> entries;

  /**
   * 构造成功结果
   */
  public static SandboxFileSearchResult success(List<String> entries) {
    return new SandboxFileSearchResult(true, null, entries);
  }

  /**
   * 构造失败结果
   */
  public static SandboxFileSearchResult fail(String errorMessage) {
    return new SandboxFileSearchResult(false, errorMessage, null);
  }
}
