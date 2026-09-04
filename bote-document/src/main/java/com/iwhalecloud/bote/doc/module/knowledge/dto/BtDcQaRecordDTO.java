package com.iwhalecloud.bote.doc.module.knowledge.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.iwhalecloud.bote.doc.module.knowledge.entity.BtDcQaRecordEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 问答记录表 DTO
 *
 * @author linmengfan
 * @since 2025-09-13
 */
@Getter
@Setter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
public class BtDcQaRecordDTO extends BtDcQaRecordEntity {
  @Schema(description = "问答涉及的知识库")
  private List<BtDcQaRecorddKnowledgeDTO> knowledges;
  @Schema(description = "提问人")
  private String creatorName;
  @Schema(description = "知识库名称")
  private String knowledgeName;
}
