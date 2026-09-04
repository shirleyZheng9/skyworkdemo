package com.iwhalecloud.bote.doc.module.knowledge.dto.query;

import java.util.List;

import com.iwhalecloud.bote.doc.common.model.PageParams;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 知识库权限表查询参数
 *
 * @author linmengfan
 * @since 2025-09-13
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "知识库权限表查询参数")
public class BtDcQaRecordQueryParams extends PageParams {
  @Schema(description = "知识库id列表")
  private List<Long> knowledgeIds;
  @Schema(description = "登陆人id")
  private Long userId;
  @Schema(description = "部门ID")
  private List<Long> deptIds;
  @Schema(description = "是否超级管理员")
  private Boolean superAdmin;
}
