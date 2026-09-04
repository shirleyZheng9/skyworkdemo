package com.iwhalecloud.bote.loop.client.evaluation.domain.common;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 消息数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "消息数据传输对象")
public class MessageDTO {

  @Schema(description = "角色")
  private RoleDTO role;

  @Schema(description = "内容")
  private ContentDTO content;

  @Schema(description = "扩展字段")
  private Map<String, String> ext;
}
