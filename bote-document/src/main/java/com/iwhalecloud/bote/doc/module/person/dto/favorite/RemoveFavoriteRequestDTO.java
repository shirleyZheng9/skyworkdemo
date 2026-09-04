package com.iwhalecloud.bote.doc.module.person.dto.favorite;

import com.iwhalecloud.bote.doc.common.model.TenantBaseRO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 取消收藏请求 DTO
 *
 * @author lizuyin
 * @since 2025-08-21
 */
@Getter
@Setter
@ToString
@Schema(description = "取消收藏请求")
public class RemoveFavoriteRequestDTO extends TenantBaseRO {

  @NotNull(message = "目标资源ID不能为空")
  @Schema(description = "目标资源ID")
  private String targetId;

  @NotNull(message = "目标类型不能为空")
  @Schema(description = "目标类型：DOCUMENT-文档，FOLDER-文件夹，LIBRARY-文档库，KNOWLEDGE-知识库")
  private String targetType;
}

