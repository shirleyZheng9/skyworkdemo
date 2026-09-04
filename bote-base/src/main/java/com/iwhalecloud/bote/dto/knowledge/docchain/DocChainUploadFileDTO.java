package com.iwhalecloud.bote.dto.knowledge.docchain;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 用于承接上传docchain之后的数据
 */
@Getter
@Setter
@ToString
public class DocChainUploadFileDTO {
  @Schema(description = "文档库文档id和docchain对应文档id的映射")
  private Map<String, Long> docIds;
  @Schema(description = "文档库文档id和对应文档大小的映射")
  private Map<String, Long> fileSizes;
}
