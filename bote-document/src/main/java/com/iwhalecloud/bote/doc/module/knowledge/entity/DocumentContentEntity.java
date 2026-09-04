package com.iwhalecloud.bote.doc.module.knowledge.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 语料基本信息 Entity
 *
 * @author auto
 * @since 2025-03-01
 */
@Getter
@Setter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
@DiffNode(name = "BT_DOCUMENT_CONTENT")
public class DocumentContentEntity extends BaseEntity {

  @DiffId
  @Schema(description = "主键")
  private Long contentId;
  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户ID")
  private Long tenantId;
  @DiffField(name = "DOCUMENT_ID")
  @Schema(description = "文档ID")
  private Long documentId;
  @DiffField(name = "SORT")
  @Schema(description = "排序")
  private Integer sort;
  @DiffField(name = "VARCHAR_TINY_COL_1")
  private String varcharTinyCol1;
  @DiffField(name = "VARCHAR_TINY_COL_2")
  private String varcharTinyCol2;
  @DiffField(name = "VARCHAR_TINY_COL_3")
  private String varcharTinyCol3;
  @DiffField(name = "VARCHAR_TINY_COL_4")
  private String varcharTinyCol4;
  @DiffField(name = "VARCHAR_TINY_COL_5")
  private String varcharTinyCol5;
  @DiffField(name = "VARCHAR_TINY_COL_6")
  private String varcharTinyCol6;
  @DiffField(name = "VARCHAR_TINY_COL_7")
  private String varcharTinyCol7;
  @DiffField(name = "VARCHAR_TINY_COL_8")
  private String varcharTinyCol8;
  @DiffField(name = "VARCHAR_TINY_COL_9")
  private String varcharTinyCol9;
  @DiffField(name = "VARCHAR_TINY_COL_10")
  private String varcharTinyCol10;
  @DiffField(name = "VARCHAR_SMALL_COL_1")
  private String varcharSmallCol1;
  @DiffField(name = "VARCHAR_SMALL_COL_2")
  private String varcharSmallCol2;
  @DiffField(name = "VARCHAR_SMALL_COL_3")
  private String varcharSmallCol3;
  @DiffField(name = "VARCHAR_SMALL_COL_4")
  private String varcharSmallCol4;
  @DiffField(name = "VARCHAR_SMALL_COL_5")
  private String varcharSmallCol5;
  @DiffField(name = "VARCHAR_SMALL_COL_6")
  private String varcharSmallCol6;
  @DiffField(name = "VARCHAR_SMALL_COL_7")
  private String varcharSmallCol7;
  @DiffField(name = "VARCHAR_SMALL_COL_8")
  private String varcharSmallCol8;
  @DiffField(name = "VARCHAR_SMALL_COL_9")
  private String varcharSmallCol9;
  @DiffField(name = "VARCHAR_SMALL_COL_10")
  private String varcharSmallCol10;
  @DiffField(name = "VARCHAR_MEDIUM_COL_1")
  private String varcharMediumCol1;
  @DiffField(name = "VARCHAR_MEDIUM_COL_2")
  private String varcharMediumCol2;
  @DiffField(name = "VARCHAR_MEDIUM_COL_3")
  private String varcharMediumCol3;
  @DiffField(name = "VARCHAR_MEDIUM_COL_4")
  private String varcharMediumCol4;
  @DiffField(name = "VARCHAR_MEDIUM_COL_5")
  private String varcharMediumCol5;
  @DiffField(name = "VARCHAR_MEDIUM_COL_6")
  private String varcharMediumCol6;
  @DiffField(name = "VARCHAR_MEDIUM_COL_7")
  private String varcharMediumCol7;
  @DiffField(name = "VARCHAR_MEDIUM_COL_8")
  private String varcharMediumCol8;
  @DiffField(name = "VARCHAR_MEDIUM_COL_9")
  private String varcharMediumCol9;
  @DiffField(name = "VARCHAR_MEDIUM_COL_10")
  private String varcharMediumCol10;
  @DiffField(name = "VARCHAR_LARGE_COL_1")
  private String varcharLargeCol1;
  @DiffField(name = "VARCHAR_LARGE_COL_2")
  private String varcharLargeCol2;
  @DiffField(name = "VARCHAR_LARGE_COL_3")
  private String varcharLargeCol3;
  @DiffField(name = "VARCHAR_LARGE_COL_4")
  private String varcharLargeCol4;
  @DiffField(name = "VARCHAR_LARGE_COL_5")
  private String varcharLargeCol5;
  @DiffField(name = "VARCHAR_LARGE_COL_6")
  private String varcharLargeCol6;
  @DiffField(name = "VARCHAR_LARGE_COL_7")
  private String varcharLargeCol7;
  @DiffField(name = "VARCHAR_LARGE_COL_8")
  private String varcharLargeCol8;
  @DiffField(name = "VARCHAR_LARGE_COL_9")
  private String varcharLargeCol9;
  @DiffField(name = "VARCHAR_LARGE_COL_10")
  private String varcharLargeCol10;
  @DiffField(name = "TEXT_COL_1")
  private String textCol1;
  @DiffField(name = "TEXT_COL_2")
  private String textCol2;
  @DiffField(name = "TEXT_COL_3")
  private String textCol3;
  @DiffField(name = "TEXT_COL_4")
  private String textCol4;
  @DiffField(name = "TEXT_COL_5")
  private String textCol5;
  @DiffField(name = "TEXT_COL_6")
  private String textCol6;
  @DiffField(name = "TEXT_COL_7")
  private String textCol7;
  @DiffField(name = "TEXT_COL_8")
  private String textCol8;
  @DiffField(name = "TEXT_COL_9")
  private String textCol9;
  @DiffField(name = "TEXT_COL_10")
  private String textCol10;
  @DiffField(name = "STATUS_TIME")
  private Date statusTime;
}

