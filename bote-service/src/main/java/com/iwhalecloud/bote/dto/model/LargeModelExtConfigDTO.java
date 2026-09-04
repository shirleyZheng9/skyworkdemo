package com.iwhalecloud.bote.dto.model;

import com.iwhalecloud.bote.llm.client.dto.HeaderItem;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 大模型扩展配置
 *
 * @author bianjp
 * @since 2025-03-14
 */
@Getter
@Setter
@ToString
@Schema(description = "大模型扩展配置")
public class LargeModelExtConfigDTO {
  @Schema(description = "请求头")
  private List<HeaderItem> headers;
  @Schema(description = "协议扩展配置（具体内容取决于协议类型）")
  private Map<String, Object> protocolExt;
  @Schema(description = "扩展请求参数")
  private String extReqParamsJson;
}
