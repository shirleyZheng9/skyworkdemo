package com.iwhalecloud.bote.doc.module.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 在线文档类型信息
 *
 * @author Aiqing
 * @since 2026/1/10
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OnlineDocumentTypeInfoDTO {

  @Schema(description = "文档类型名称")
  private String documentTypeName;
  @Schema(description = "文档类型编码")
  private String documentType;
}
