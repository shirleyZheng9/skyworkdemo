package com.iwhalecloud.bote.doc.module.collaboration.doc.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 导入在线文档内容结果
 *
 * @author lizuyin
 * @since 2025/10/22
 */
@Getter
@Setter
@ToString
public class DocumentImportResult {

  @Schema(description = "结果码")
  private Integer resultCode;
  @Schema(description = "结果消息")
  private String resultMsg;
}

