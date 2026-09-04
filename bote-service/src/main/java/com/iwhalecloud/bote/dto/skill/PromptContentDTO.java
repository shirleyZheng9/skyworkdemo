package com.iwhalecloud.bote.dto.skill;

import com.iwhalecloud.bote.entity.skill.PromptContentEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 提示词内容
 *
 * @author qian.sisheng
 * @since 2025-2-13
 */
@Getter
@Setter
@ToString(callSuper = true)
public class PromptContentDTO extends PromptContentEntity {
  @Schema(description = "模型名称")
  private String modelName;
}
