package com.iwhalecloud.bote.loop.prompt.domain.repo.dto;

import com.iwhalecloud.bote.loop.prompt.domain.entity.Prompt;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 列表查询Prompt结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListPromptResult {
  @Schema(description = "总记录数")
  private Long total;
  @Schema(description = "Prompt列表")
  private List<Prompt> promptDOs;
}
