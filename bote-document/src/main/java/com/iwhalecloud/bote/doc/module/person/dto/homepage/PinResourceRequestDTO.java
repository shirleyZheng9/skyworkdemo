package com.iwhalecloud.bote.doc.module.person.dto.homepage;

import com.iwhalecloud.bote.doc.common.model.TenantBaseRO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 置顶资源请求参数
 *
 * @author lizuyin
 * @since 2025-01-18
 */
@Getter
@Setter
@ToString
@Schema(description = "置顶资源请求参数")
public class PinResourceRequestDTO extends TenantBaseRO {

  @Schema(description = "目标资源ID")
  @NotBlank(message = "目标资源ID不能为空")
  private String targetId;

  @Schema(description = "目标类型：DOCUMENT-文档，LIBRARY-文档库，KNOWLEDGE-知识库")
  @NotBlank(message = "目标类型不能为空")
  private String targetType;
}
