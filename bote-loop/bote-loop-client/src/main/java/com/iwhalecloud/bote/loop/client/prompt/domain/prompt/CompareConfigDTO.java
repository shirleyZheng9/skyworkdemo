package com.iwhalecloud.bote.loop.client.prompt.domain.prompt;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 比较配置DTO
 * 迁移对应关系: Thrift struct CompareConfig
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompareConfigDTO {

  @Schema(description = "比较组列表")
  private List<CompareGroupDTO> groups;
}
