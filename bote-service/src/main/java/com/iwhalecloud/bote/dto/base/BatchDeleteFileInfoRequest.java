package com.iwhalecloud.bote.dto.base;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 批量删除文件信息请求
 *
 * @author auto
 * @since 2024-09-24
 */
@Getter
@Setter
@ToString
@Schema(description = "批量删除文件信息请求")
public class BatchDeleteFileInfoRequest {

  @Schema(description = "租户 ID ")
  private Long tenantId;

  @Schema(description = "文件信息ID列表")
  private List<Long> fileInfoIds;
}
