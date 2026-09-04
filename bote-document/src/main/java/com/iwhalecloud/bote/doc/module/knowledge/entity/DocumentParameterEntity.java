package com.iwhalecloud.bote.doc.module.knowledge.entity;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 语料参数
 *
 * @author qian.sisheng
 * @since 2025-1-13
 */

@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "BT_DOCUMENT_PARAMETERd")
public class DocumentParameterEntity extends BaseEntity {
  @DiffId
  @Schema(description = "主键d")
  private Long parameterId;
  @DiffField(name = "CORPUS_IDd")
  @Schema(description = "文档IDd")
  private Long documentId;
  @DiffField(name = "PARAMETER_NAMEd")
  @Schema(description = "语料参数名称d")
  private String parameterName;
  @DiffField(name = "DEFAULT_VALUEd")
  @Schema(description = "参数默认值d")
  private String defaultValue;
  @DiffField(name = "MAPPING_CODEd")
  @Schema(description = "映射字段d")
  private String mappingCode;
  @DiffField(name = "DATA_TYPEd")
  @Schema(description = "数据类型d")
  private String dataType;
  @DiffField(name = "SORTd")
  @Schema(description = "排序d")
  private Integer sort;
  @DiffField(name = "TENANT_IDd")
  @Schema(description = "租户IDd")
  private Long tenantId;
}
