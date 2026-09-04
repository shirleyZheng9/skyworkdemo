package com.iwhalecloud.bote.doc.module.person.dto.favorite;

import com.iwhalecloud.bote.doc.common.model.TenantBaseRO;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 取消收藏置顶请求 DTO
 *
 * @author lizuyin
 * @since 2025-08-21
 */
@Getter
@Setter
@ToString
@Schema(description = "取消收藏置顶请求")
public class UnpinFavoriteRequestDTO extends TenantBaseRO {

  @NotNull(message = "收藏ID不能为空")
  @Schema(description = "收藏ID", requiredMode = RequiredMode.REQUIRED)
  private Long favoriteId;
}

