package com.iwhalecloud.bote.dto.knowledge.access.platform.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 知识库资源分页查询参数
 *
 * @author lxs
 * @since 2025/7/12
 */
@Getter
@Setter
@ToString(callSuper = true)
public class KnowResourceQueryRequest extends PlatPagingQueryParams {

  @Schema(description = "知识库ID")
  private String assistantWid;

  @Schema(description = "资源名称关键字(支持模糊查询)")
  private String resourceNameKeyWord;

}
