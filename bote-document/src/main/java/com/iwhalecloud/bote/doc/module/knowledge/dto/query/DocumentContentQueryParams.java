package com.iwhalecloud.bote.doc.module.knowledge.dto.query;


import com.iwhalecloud.bote.doc.common.model.PageParams;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文档内容查询参数
 *
 * @author qian.sisheng
 * @since 2025-3-12
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "语料基本信息查询参数")
public class DocumentContentQueryParams extends PageParams {
  @Schema(description = "模糊查询")
  private String searchContent;
  @Schema(description = "文档ID")
  private Long documentId;
  @Schema(description = "文档ID集合")
  private List<Long> documentIds;
  @Schema(description = "租户ID")
  private Long tenantId;
}
