package com.iwhalecloud.bote.dto.knowledge.docchain;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * DocChain 文档引用
 */
@Getter
@Setter
@ToString
public class DocChainReferenceDTO {
  /** 引用的文档列表 */
  private List<DocChainDocDTO> references;
  /** 引用的图片链接列表 */
  private List<String> referenceImages;
}
