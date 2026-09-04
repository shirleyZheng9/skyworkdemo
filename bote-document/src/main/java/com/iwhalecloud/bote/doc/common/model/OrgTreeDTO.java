package com.iwhalecloud.bote.doc.common.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 组织结构树信息
 *
 * @author Aiqing
 * @since 2025/8/18
 */
@Getter
@Setter
@ToString
public class OrgTreeDTO extends OrgDTO {

  @Schema(description = "下级组织")
  private List<OrgTreeDTO> children;
}
