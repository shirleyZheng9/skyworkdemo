package com.iwhalecloud.bote.doc.module.person.dto.favorite;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 添加收藏响应 DTO
 *
 * @author lizuyin
 * @since 2025-08-21
 */
@Getter
@Setter
@ToString
@Schema(description = "添加收藏响应")
public class AddFavoriteResponseDTO {

  @Schema(description = "收藏ID")
  private Long favoriteId;
}

