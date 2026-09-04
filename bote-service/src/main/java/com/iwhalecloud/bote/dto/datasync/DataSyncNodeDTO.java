package com.iwhalecloud.bote.dto.datasync;

import com.iwhalecloud.bote.dto.base.CatalogTree;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 数据同步节点
 *
 * @author chen.linfa
 * @since 2025-01-14
 */
@Getter
@Setter
@ToString
public class DataSyncNodeDTO {
  @Schema(description = "编码")
  private String code;
  @Schema(description = "名称")
  private String name;
  @Schema(description = "父编码")
  private String parentCode;
  @Schema(description = "父名称")
  private String parentName;
  @Schema(description = "表编码")
  private String tableCode;
  @Schema(description = "主键字段")
  private String primaryColumn;
  @Schema(description = "编码字段")
  private String codeColumn;
  @Schema(description = "名称字段")
  private String nameColumn;
  @Schema(description = "查询条件")
  private String queryCondition;
  @Schema(description = "配置数据是否带有目录")
  private String catalogFlag;
  @Schema(description = "配置数据是否带有目录")
  private String catalogType;

  @Schema(description = "配置数据")
  private List<Map<String, Object>> records;
  @Schema(description = "树形结构配置数据")
  private List<CatalogTree<Map<String, Object>>> treeRecords;
  @Schema(description = "总数")
  private Integer total;
  @Schema(description = "模块排序")
  private Integer groupSortby;
}
