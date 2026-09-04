package com.iwhalecloud.bote.doc.module.document.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author Aiqing
 * @since 2025/9/29
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ExportDocumentFileDTO {

  /**
   * 文件名称
   */
  private String fileName;
  /**
   * 文件大小
   */
  private Long fileSize;
  /**
   * 文件扩展名
   */
  private String fileExtension;
}
