package com.iwhalecloud.bote.doc.module.document.dto;

import com.iwhalecloud.bote.doc.common.model.PortalUserDTO;
import com.iwhalecloud.bote.doc.module.control.vo.NodePermissionView;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文档详情
 *
 * @author Aiqing
 * @since 2025/9/9
 */
@Getter
@Setter
@ToString
public class DcDocumentDetailDTO extends DcDocumentDTO {

  @Schema(description = "文档路径")
  private DocumentPathDTO documentPath;

  @Schema(description = "角色编码", example = "editor")
  private String permission;

  @Schema(description = "权限点")
  private NodePermissionView permissionView;

  @Schema(description = "贡献者")
  private List<DocumentContributorDTO> contributors;

  @Schema(description = "创建人")
  private PortalUserDTO creator;
  @Schema(description = "最后编辑人")
  private PortalUserDTO updator;

}
