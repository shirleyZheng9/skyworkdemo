package com.iwhalecloud.bote.doc.module.collaboration.workbook.ro;

import com.iwhalecloud.bote.doc.common.model.TenantBaseRO;
import com.iwhalecloud.bote.doc.module.collaboration.workbook.dto.ChangesetDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 表格数据变更请求参数
 *
 * @author Aiqing
 * @since 2025/9/2
 */
@Getter
@Setter
@ToString
public class NewChangesRO extends TenantBaseRO {

  @Schema(description = "表格ID")
  @NotEmpty(message = "表格ID不能为空")
  private String unitID;

  @Schema(description = "变更集")
  private ChangesetDTO changeset;

}
