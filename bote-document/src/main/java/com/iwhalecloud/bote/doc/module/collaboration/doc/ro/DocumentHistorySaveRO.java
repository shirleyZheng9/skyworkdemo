package com.iwhalecloud.bote.doc.module.collaboration.doc.ro;

import com.iwhalecloud.bote.doc.module.document.dto.YdocContent;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 在线文档历史快照请求
 *
 * @author Aiqing
 * @since 2025/8/30
 */
@Getter
@Setter
@ToString
public class DocumentHistorySaveRO {

  @Schema(description = "文档ID")
  private String documentId;
  @Schema(description = "更新内容")
  private YdocContent content;
  @Schema(description = "更新人")
  private Long updatorId;
}
