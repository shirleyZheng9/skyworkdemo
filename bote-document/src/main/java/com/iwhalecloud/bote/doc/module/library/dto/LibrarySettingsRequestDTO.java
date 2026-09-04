package com.iwhalecloud.bote.doc.module.library.dto;

import com.iwhalecloud.bote.doc.common.model.TenantBaseRO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Schema(description = "文档库设置请求")
public class LibrarySettingsRequestDTO extends TenantBaseRO {

  @NotBlank(message = "操作类型不能为空")
  @Schema(description = "操作类型：UPDATE_INFO-更新基础信息，UPDATE_VISIBILITY-更新可见范围")
  private String action;

  @NotNull(message = "设置数据不能为空")
  @Schema(description = "设置数据")
  private LibrarySettingsDataDTO data;

  @Getter
  @Setter
  @ToString
  @Schema(description = "文档库设置数据")
  public static class LibrarySettingsDataDTO {

    @Schema(description = "文档库名称")
    private String libraryName;
    @Schema(description = "描述")
    private String description;
    @Schema(description = "文档库图标")
    private String libraryIcon;
    @Schema(description = "文档库图标颜色")
    private String color;
    @Schema(description = "可见范围：PUBLIC-全员可见，MEMBERS-成员可见，PRIVATE-私有，OTHER-其他")
    private String visibilityScope;
  }
}
