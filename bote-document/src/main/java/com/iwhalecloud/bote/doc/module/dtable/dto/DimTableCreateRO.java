package com.iwhalecloud.bote.doc.module.dtable.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 创建多维表文档请求参数
 *
 * @author Aiqing
 * @since 2026/1/9
 */
@Getter
@Setter
@ToString
public class DimTableCreateRO {

  @Schema(description = "space name")
  @NotEmpty(message = "space name不能为空")
  private String name;
  @Schema(description = "ownerId")
  @NotNull(message = "ownerId不能为空")
  private Long ownerId;
  @Schema(description = "空间类型")
  private String spaceType;
  @NotNull(message = "绑定文档ID不能为空")
  private String bindEntityId;
}
