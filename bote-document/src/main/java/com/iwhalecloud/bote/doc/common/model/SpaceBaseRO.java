package com.iwhalecloud.bote.doc.common.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 企业空间请求参数的基础类
 *
 * @author Aiqing
 * @since 2025/10/24
 */
@Getter
@Setter
@ToString
public class SpaceBaseRO {

  @Schema(description = "企业空间ID")
  protected Long spaceId;

}
