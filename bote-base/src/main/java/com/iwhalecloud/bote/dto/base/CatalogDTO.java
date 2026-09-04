package com.iwhalecloud.bote.dto.base;

import com.iwhalecloud.bote.entity.base.CatalogEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 目录 DTO
 *
 * @author auto
 * @since 2024-09-13
 */
@Getter
@Setter
@ToString(callSuper = true)
public class CatalogDTO extends CatalogEntity {
  @Schema(description = "修改人名称")
  private String updatorName;
  @Schema(description = "子节点")
  private List<CatalogDTO> children;
}
