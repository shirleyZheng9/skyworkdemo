package com.iwhalecloud.bote.dto.model;

import com.iwhalecloud.bote.entity.model.LargeModelEntity;
import com.iwhalecloud.bote.llm.client.dto.HeaderItem;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 大模型 DTO
 *
 * @author auto
 * @since 2024-09-20
 */
@Getter
@Setter
@ToString(callSuper = true)
public class LargeModelDTO extends LargeModelEntity {
  @Schema(description = "模型来源：platform/tenant/gateway")
  private String sourceFrom;
  @Schema(description = "请求头")
  private List<HeaderItem> headers;
  @Schema(description = "协议扩展配置")
  private Map<String, Object> protocolExt;
  @Schema(description = "扩展请求参数")
  private String extReqParamsJson;
  @Schema(description = "修改人名称")
  private String updatorName;
  @Schema(description = "企业空间ID")
  private Long spaceId;
}
