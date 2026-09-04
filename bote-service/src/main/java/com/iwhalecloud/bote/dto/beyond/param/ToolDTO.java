package com.iwhalecloud.bote.dto.beyond.param;

import com.iwhalecloud.bote.llm.client.dto.schema.JsonSchemaNode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 工具参数DTO
 *
 * @author lizuyin
 * @since 2025-07-21
 */
@Getter
@Setter
@ToString
public class ToolDTO {
  /** 工具入参 */
  private JsonSchemaNode inputSchema;
  /** 工具出参 */
  private JsonSchemaNode outputSchema;
  /** 工具原始地址 */
  private String urlOri;
  /** 传输类型 */
  private String transferType;
  /** 方法 */
  private String method;
  /** 路径参数 */
  private JsonSchemaNode pathSchema;
  /** 查询参数 */
  private JsonSchemaNode querySchema;
}
