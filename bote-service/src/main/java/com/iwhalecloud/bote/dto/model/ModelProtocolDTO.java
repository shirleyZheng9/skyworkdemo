package com.iwhalecloud.bote.dto.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 大模型协议
 *
 * @author bianjp
 * @since 2025-05-13
 */
@Getter
@Setter
@ToString
@Schema(description = "大模型协议")
@NoArgsConstructor
@AllArgsConstructor
public class ModelProtocolDTO {
  @Schema(description = "协议名称")
  private String name;
  @Schema(description = "协议编码")
  private String code;
  @Schema(description = "协议描述")
  private String description;
  @Schema(description = "配置属性列表，为空时表示使用默认配置")
  private List<Object> attrs;
}
