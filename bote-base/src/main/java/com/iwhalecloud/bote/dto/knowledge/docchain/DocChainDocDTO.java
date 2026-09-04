package com.iwhalecloud.bote.dto.knowledge.docchain;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * DocChain 引用的文档
 */
@Getter
@Setter
@ToString
public class DocChainDocDTO {
  /** 文档名称 */
  private String title;
  /** 文档切片信息列表 */
  private List<DocChainLinkDTO> links;
}
