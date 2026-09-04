package com.iwhalecloud.bote.doc.module.knowledge.dto.query;

import com.iwhalecloud.bote.doc.common.model.PageParams;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文档查询参数
 *
 * @author auto
 * @since 2024-09-20
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "文档查询参数")
public class DocumentQueryParams extends PageParams {
  @Schema(description = "租户 ID")
  private Long tenantId;
  @Schema(description = "模糊查询")
  private String searchContent;
  @Schema(description = "知识库ID")
  private Long knowledgeId;
  @Schema(description = "目录ID")
  private Long catalogItemId;
  @Schema(description = "按创建时间排序：desc降序 asc升序")
  private String sort;
  @Schema(description = "文档ID列表")
  private List<Long> documentIds;
  @Schema(description = "目录ID列表")
  private List<Long> catalogItemList;
  @Schema(description = "外系统 ID (DocChain 的文档 ID), 智能测试平台使用")
  private Long extSystemId;

  @Schema(description = "文档类型：WORD-Word文档，EXCEL-Excel表格,FOLDER-文件夹等")
  private String dcDocumentType;

  @Schema(description = "文档类型：WORD-Word文档，EXCEL-Excel表格,FOLDER-文件夹等")
  private List<String> dcDocumentTypes;

  @Schema(description = "状态: 10A未处理 10B解析中 10C要素抽取中 10D构建完成 10E构建失败")
  private String docStatus;

  @Schema(description = "状态: 10A未处理 10B解析中 10C要素抽取中 10D构建完成 10E构建失败")
  private List<String> docStatuss;

  @Schema(description = "知识库 ID 集合（支持引用变量，支持逗号分隔的多个 ID)")
  private String knowledgeIdExpr;

  @Schema(description = "是否有更新：0-无更新，1-有更新")
  private String hasUpdate;

}

