package com.iwhalecloud.bote.doc.module.knowledge.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文档切片数据
 *
 * @author qian.sisheng
 * @since 2025-07-03
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
@Schema(description = "参考文档")
public class ReferenceChunkResponse {
  /** 数据 */
  private ReferenceChunkResponseData data;
}
