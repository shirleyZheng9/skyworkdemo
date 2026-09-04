package com.iwhalecloud.bote.doc.module.document.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.iwhalecloud.bote.doc.module.document.entity.DocContentHistoryEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文档版本
 *
 * @author yangran
 * @since 2025-08-13
 */
@Getter
@Setter
@ToString(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "文档版本")
public class DocContentHistoryDTO extends DocContentHistoryEntity {
  @Schema(description = "创建人名称（bt_user.real_name）")
  private String userName;
}
