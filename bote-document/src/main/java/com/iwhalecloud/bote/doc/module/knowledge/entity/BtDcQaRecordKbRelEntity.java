package com.iwhalecloud.bote.doc.module.knowledge.entity;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 问答记录知识库关系表 Entity
 *
 * @author linmengfan
 * @since 2025-09-15
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "BT_DC_QA_RECORD_KB_REL")
public class BtDcQaRecordKbRelEntity extends BaseEntity {

  @DiffId
  @Schema(description = "主键")
  private Long relId;
  @DiffField(name = "QA_ID")
  @Schema(description = "问答记录ID")
  private Long qaId;
  @DiffField(name = "KB_ID")
  @Schema(description = "知识库ID")
  private Long kbId;
  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户 ID")
  private Long tenantId;
}
