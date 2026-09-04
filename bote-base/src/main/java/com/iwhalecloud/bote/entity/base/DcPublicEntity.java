package com.iwhalecloud.bote.entity.base;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 配置词典 Entity
 *
 * @author auto
 * @since 2024-09-14
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_dc_public")
public class DcPublicEntity extends BaseEntity {
  @DiffId
  @Schema(description = "主键")
  private String systemid;
  @DiffField(name = "STYPE")
  private Long stype;
  @DiffField(name = "PKEY")
  @Schema(description = "key")
  private String pkey;
  @DiffField(name = "PCODE")
  @Schema(description = "编码")
  private String pcode;
  @DiffField(name = "PNAME")
  @Schema(description = "名称")
  private String pname;
  @DiffField(name = "COMMENTS")
  @Schema(description = "描述")
  private String comments;
  @DiffField(name = "CODEA")
  @Schema(description = "预留字段1")
  private String codea;
  @DiffField(name = "CODEB")
  @Schema(description = "预留字段2")
  private String codeb;
  @DiffField(name = "CODEC")
  @Schema(description = "预留字段3")
  private String codec;
  @DiffField(name = "CODED")
  @Schema(description = "预留字段4")
  private String coded;
  @DiffField(name = "CODEE")
  @Schema(description = "预留字段5")
  private String codee;
  @DiffField(name = "SORTBY")
  @Schema(description = "排序")
  private Integer sortby;
}
