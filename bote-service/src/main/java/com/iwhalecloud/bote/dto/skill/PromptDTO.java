package com.iwhalecloud.bote.dto.skill;

import com.iwhalecloud.bote.entity.skill.PromptEntity;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 提示词 DTO
 *
 * @author qian.sisheng
 * @since 2024/8/2
 */
@Getter
@Setter
@ToString(callSuper = true)
public class PromptDTO extends PromptEntity {
  @Schema(description = "修改人名称")
  private String updatorName;
  @Schema(description = "修改人名称")
  private String creatorName;
  @Schema(description = "提示词内容列表")
  @DiffField(childNode = true)
  private List<PromptContentDTO> promptContents;
}
