package com.iwhalecloud.bote.doc.module.knowledge.dto;

import com.iwhalecloud.bote.doc.common.model.TenantBaseRO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UpdateKnowlegeVisibilityScopeDTO extends TenantBaseRO {
  @Schema(description = "租户 ID")
  private Long tenantId;
  @Schema(description = "知识库 ID")
  private Long knowledgeId;
  @Schema(description = "操作人 ID")
  private Long userId;
  @Schema(description = "可见范围：PRIVATE-私有，PUBLIC-全员可见，MEMBERS-成员可见")
  private String visibilityScope;
}
