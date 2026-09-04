package com.iwhalecloud.bote.doc.module.person.dto.homepage;

import com.iwhalecloud.bote.doc.common.model.TenantBaseRO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 从最近访问列表中移除文件请求参数
 *
 * @author lizuyin
 * @since 2025-08-28
 */
@Getter
@Setter
@ToString
@Schema(description = "从最近访问列表中移除文件请求参数")
public class RemoveFromRecentFilesRequestDTO extends TenantBaseRO {

  @Schema(description = "文档ID")
  private String documentId;
  @Schema(description = "是否在操作Mapper后删除源文件")
  private Boolean deleteSourceAfter;
}

