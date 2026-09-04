package com.iwhalecloud.bote.doc.module.collaboration.workbook.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author Aiqing
 * @since 2025/9/2
 */
@Getter
@Setter
@ToString
public class UniverResultVO {

  @Schema(description = "接口状态字段")
  private ErrorResult error;

  public ErrorResult successResult() {
    ErrorResult errorResult = new ErrorResult();
    errorResult.setCode(1);
    errorResult.setMessage("");
    return errorResult;
  }

  @Getter
  @Setter
  @ToString
  public static class ErrorResult {
    @Schema(description = "状态码，1为成功")
    private Integer code;
    @Schema(description = "错误信息")
    private String message;
  }
}
