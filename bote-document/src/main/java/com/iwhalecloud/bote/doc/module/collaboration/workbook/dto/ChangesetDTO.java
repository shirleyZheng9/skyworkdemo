package com.iwhalecloud.bote.doc.module.collaboration.workbook.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 变更集
 *
 * @author qingai
 * @since 2025-09-02
 */
@Getter
@Setter
@ToString
@Schema(description = "变更集")
public class ChangesetDTO {
  @Schema(description = "文档ID")
  @NotEmpty(message = "文档ID不能为空")
  private String unitID;

  @Schema(description = "类型， 默认2为表格文档")
  private Integer type = 2;

  @Schema(description = "基础版本号")
  @NotNull(message = "基础版本号不能为空")
  private Long baseRev;

  @Schema(description = "版本号")
  @NotNull(message = "版本号不能为空")
  private Long revision;

  @Schema(description = "用户ID")
  private String userID;

  @Schema(description = "变更列表")
  @NotEmpty(message = "变更列表不能为空")
  private List<ChangesetMutationDTO> mutations;

  @Schema(description = "成员ID")
  private String memberID;

  @Schema(description = "会话ID")
  private String sid;

  @Schema(description = "请求ID")
  private Long reqId;
}
