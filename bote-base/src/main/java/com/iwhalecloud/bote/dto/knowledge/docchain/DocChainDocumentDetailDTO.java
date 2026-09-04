package com.iwhalecloud.bote.dto.knowledge.docchain;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * DocChain 文档详情
 *
 * @author bianjp
 * @since 2025-04-16
 */
@Getter
@Setter
@ToString
public class DocChainDocumentDetailDTO {
  /** 文档 ID */
  private Long id;
  /** 主题 ID */
  @JsonAlias("topic_id")  // DocChain 返回的是下划线形式
  private Long topicId;
  /** 文档路径（名称 + 扩展名） */
  private String path;
  /** 文件名称（不含扩展名） */
  @JsonAlias("base_name")
  private String baseName;
  /** 文件扩展名（以 "." 开头） */
  @JsonAlias("file_extension")
  private String fileExtension;
}
