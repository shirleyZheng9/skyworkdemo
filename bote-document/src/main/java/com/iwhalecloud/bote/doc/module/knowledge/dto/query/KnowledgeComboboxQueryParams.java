package com.iwhalecloud.bote.doc.module.knowledge.dto.query;

import com.iwhalecloud.bote.doc.common.model.PageParams;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 知识库下拉框查询条件
 *
 * @author bianjp
 * @since 2024-10-08
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "知识库下拉框查询条件")
public class KnowledgeComboboxQueryParams extends PageParams {
  @Schema(description = "知识库类型", requiredMode = Schema.RequiredMode.REQUIRED)
  private String knowledgeType;
  @Schema(description = "租户 ID")
  private Long tenantId;
  @Schema(description = "搜索关键字（模糊搜索知识库名称）")
  private String searchContent;
  @Schema(description = "是否超级管理员")
  private Boolean superAdmin;
  @Schema(description = "登陆人id")
  private Long userId;
  @Schema(description = "部门ID")
  private List<Long> deptIds;
}
