package com.iwhalecloud.bote.doc.module.document.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.iwhalecloud.bote.doc.module.document.entity.DcDocContentEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "在线文档DTO")
public class DcDocContentDTO extends DcDocContentEntity {
  @Schema(description = "文档版本号（bt_dc_document.revision，与节点保存/回退等联动递增）")
  private Long revision;
}
