package com.iwhalecloud.bote.entity.portal;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 权限定义 Entity
 *
 * @author auto
 * @since 2024-10-14
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_priv")
public class PrivEntity extends BaseEntity {
  @DiffId
  @Schema(description = "主键")
  private Long privId;
  @DiffField(name = "PRIV_TYPE")
  @Schema(description = "权限类型")
  private String privType;
  @DiffField(name = "PRIV_CODE")
  @Schema(description = "权限编码")
  private String privCode;
  @DiffField(name = "PRIV_NAME")
  @Schema(description = "权限名称")
  private String privName;
  @DiffField(name = "PRIV_URL")
  @Schema(description = "菜单地址")
  private String privUrl;
  @DiffField(name = "CATALOG_ITEM_ID")
  @Schema(description = "目录 ID")
  private Long catalogItemId;
}
