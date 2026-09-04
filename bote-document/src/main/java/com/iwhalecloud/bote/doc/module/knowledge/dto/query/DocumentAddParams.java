package com.iwhalecloud.bote.doc.module.knowledge.dto.query;

import com.iwhalecloud.bote.doc.common.model.TenantBaseRO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 新增文档参数
 *
 * @author chen.linfa
 * @since 2025-08-15
 */
@Getter
@Setter
@ToString
@Schema(description = "文档新增参数")
public class DocumentAddParams extends TenantBaseRO {
  @Schema(description = "租户 ID")
  private Long tenantId;

  @Schema(description = "知识库 ID")
  private Long knowledgeId;

  @Schema(description = "非结构化的文件关联 ID 集合")
  private List<Long> fileInfoIds;

  @Schema(description = "结构化类型的文件关联 ID 集合")
  private List<Long> structFileInfoIds;

  @Schema(description = "文件关联 ID 集合")
  private List<String> dcDocumentIds;

}
