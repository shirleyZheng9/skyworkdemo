package com.iwhalecloud.bote.dto.knowledge.docchain.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文档块拆分响应
 *
 * @author qian.sisheng
 * @since 2025-11-27
 */
@Getter
@Setter
@ToString
public class SplitDocResponse {
  /** 错误码 */
  private String code;
  /** 异常信息 */
  private String err;
  /** 结果 */
  private Data data;

  @Getter
  @Setter
  @ToString
  public static class Data {
    /** 表格列表 */
    private List<Table> tables;
  }

  /**
   * 表格信息
   */
  @Getter
  @Setter
  @ToString
  public static class Table {
    /** HTML 内容 */
    @JsonProperty("html_content")
    private String htmlContent;
  }
}
