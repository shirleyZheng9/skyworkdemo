package com.iwhalecloud.bote.doc.module.knowledge.dto.query;

import com.iwhalecloud.bote.doc.common.model.PageParams;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 语料基本信息查询参数
 *
 * @author auto
 * @since 2025-01-13
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "语料基本信息查询参数")
public class CorpusQueryParams extends PageParams {

  @Schema(description = "模糊查询")
  private String searchContent;
  @Schema(description = "按照创建时间排序，desc 降序 asc 升序")
  private String orderBy;
  @Schema(description = "租户ID")
  private Long tenantId;
  @Schema(description = "语料ID")
  private Long corpusId;
  @Schema(description = "目录ID")
  private Long catalogItemId;
  @Schema(description = "目录ID列表", hidden = true)
  private List<Long> catalogItemList;
  @Schema(description = "排序")
  private String sort;
  @Schema(description = "操作类型")
  private String corpusType;
}
