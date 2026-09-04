package com.iwhalecloud.bote.entity.knowledge;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 语料基本信息
 *
 * @author qian.sisheng
 * @since 2025-3-10
 */

@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "BT_CORPUS_INFO")
public class CorpusInfoEntity extends BaseEntity {
  @DiffId
  @Schema(description = "语料ID")
  private Long corpusId;
  @DiffField(name = "CORPUS_NAME")
  @Schema(description = "语料名称")
  private String corpusName;
  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户ID")
  private Long tenantId;
  @DiffField(name = "CATALOG_ITEM_ID")
  @Schema(description = "目录ID")
  private Long catalogItemId;
  @Schema(description = "操作类型: 文件导入import  在线配置 config")
  private String operateType;
}
