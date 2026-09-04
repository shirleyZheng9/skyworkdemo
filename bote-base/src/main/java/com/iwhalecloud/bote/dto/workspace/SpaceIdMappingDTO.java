package com.iwhalecloud.bote.dto.workspace;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 空间ID映射信息
 *
 * @author yuyuling
 * @since 2025-06-13
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
@Schema(description = "空间ID映射信息")
public class SpaceIdMappingDTO {

  @Schema(description = "企业空间ID")
  private Long spaceId;

  @Schema(description = "外系统空间ID")
  private String extSpaceId;
}
