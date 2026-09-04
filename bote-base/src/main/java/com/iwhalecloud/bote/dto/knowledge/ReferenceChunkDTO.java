package com.iwhalecloud.bote.dto.knowledge;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 参考文档块
 *
 * @author qian.sisheng
 * @since 2025-07-03
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
@Schema(description = "参考文档")
public class ReferenceChunkDTO {
  @Schema(description = "文档块 ID")
  private String chunkId;
  @Schema(description = "文档 ID")
  private String id;
  @Schema(description = "文档块名称")
  private String name;
  @Schema(description = "排序")
  private String rank;
  @Schema(description = "分数")
  private String score;
}
