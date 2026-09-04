package com.iwhalecloud.bote.entity.base;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import jakarta.validation.constraints.Size;

/**
 * 标签 Entity
 *
 * @author auto
 * @since 2024-09-13
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_label")
public class LabelEntity extends BaseEntity {
  @DiffId
  @Schema(description = "主键")
  private Long labelId;
  @DiffField(name = "LABEL_NAME")
  @Schema(description = "标签名称")
  @Size(max = 20, message = "标签名称超过限定长度20")
  private String labelName;
  @DiffField(name = "LABEL_TYPE")
  @Schema(description = "标签类型")
  private String labelType;
  @DiffField(name = "LABEL_COLOR")
  @Schema(description = "标签颜色")
  private String labelColor;
  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户 ID")
  private Long tenantId;
}
