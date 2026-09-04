package com.iwhalecloud.bote.doc.module.document.dto;

import java.util.List;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文档路径的封装
 *
 * @author Aiqing
 * @since 2025/8/15
 */
@Data
@Getter
@Setter
@ToString
public class DocumentPathDTO {

  /**
   * 文档路径拼接， 例：国内出海文档库/出海/解决方案
   */
  private String documentPath;

  /**
   * 文档路径编码, 例：lrbABC123DEF456/fodABC123DEF456
   */
  private String documentPathCode;

  /**
   * 文档路径，例：["国内出海文档库","出海", "解决方案"]
   */
  private List<NodePathDTO> path;
}
