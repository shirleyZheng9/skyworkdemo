package com.iwhalecloud.bote.doc.module.person.dto.favorite;

import com.iwhalecloud.bote.doc.common.model.TenantBaseRO;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 添加收藏请求 DTO
 *
 * @author lizuyin
 * @since 2025-08-21
 */
@Getter
@Setter
@ToString
@Schema(description = "添加收藏请求")
public class AddFavoriteRequestDTO extends TenantBaseRO {

  @NotNull(message = "目标资源ID不能为空")
  @Schema(description = "目标资源ID", requiredMode = RequiredMode.REQUIRED)
  private String targetId;

  @NotNull(message = "目标类型不能为空")
  @Schema(description = "目标类型：DOCUMENT-文档，FOLDER-文件夹，LIBRARY-文档库，KNOWLEDGE-知识库",
    requiredMode = RequiredMode.REQUIRED)
  private String targetType;
}
