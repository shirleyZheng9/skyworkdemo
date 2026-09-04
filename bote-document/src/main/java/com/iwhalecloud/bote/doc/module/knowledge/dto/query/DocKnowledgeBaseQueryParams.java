package com.iwhalecloud.bote.doc.module.knowledge.dto.query;

import com.iwhalecloud.bote.doc.common.model.PageParams;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 知识库查询参数
 *
 * @author yangran
 * @since 2025-08-13
 */
@Getter
@Setter
@ToString
@Schema(description = "知识库查询参数")
public class DocKnowledgeBaseQueryParams extends PageParams {
  @Schema(description = "模糊查询")
  private String searchContent;
  @Schema(description = "知识库状态: 10A未发布 10B发布中 10C已修改 10D已发布 10E发布失败")
  private String knowledgeStatus;
  @Schema(description = "知识库类型")
  private String knowledgeType;
  @Schema(description = "按创建时间排序：desc降序 asc升序")
  private String sort;
  @Schema(description = "租户ID")
  private Long tenantId;
  @Schema(description = "租户ID 不等于谁")
  private Long unTenantId;
  @Schema(description = "登陆人id")
  private Long userId;
  @Schema(description = "部门ID")
  private List<Long> deptIds;
  @Schema(description = "目录ID")
  private Long catalogItemId;
  @Schema(description = "是否超级管理员")
  private Boolean superAdmin;
  @Schema(description = "目录ID列表", hidden = true)
  private List<Long> catalogItemList;

  @Schema(description = "租户ID置顶收藏的")
  private Long pinTenantId;

  @Schema(description = "ai门户是否为企业，T为企业，F为项目，空则是开发项目")
  private String enterprise;
}

