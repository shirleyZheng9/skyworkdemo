package com.iwhalecloud.bote.dto.base;

import com.iwhalecloud.bote.entity.base.PublishStepEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 发布步骤信息 DTO
 *
 * @author auto
 * @since 2024-10-21
 */
@Getter
@Setter
@ToString(callSuper = true)
public class PublishStepDTO extends PublishStepEntity {
  @Schema(description = "输出信息")
  private String output;
}
