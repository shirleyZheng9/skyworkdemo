package com.iwhalecloud.bote.doc.common.utils.converter;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Word 文档加载结果 DTO：封装已加载的 {@link WordprocessingMLPackage}
 * 以及导出完成后执行的清理逻辑（如删除临时文件），确保 docx4j 导出时从已清洗文件读取。
 */
@Getter
@Setter
@ToString
public class WordPackageLoadResultDTO {

  private WordprocessingMLPackage pkg;

  private Runnable cleanup;

  public WordPackageLoadResultDTO(WordprocessingMLPackage pkg, Runnable cleanup) {
    this.pkg = pkg;
    this.cleanup = cleanup;
  }
}

