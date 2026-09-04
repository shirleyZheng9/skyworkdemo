package com.iwhalecloud.bote.entity.base;

import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 敏感词
 *
 * @author bianjp
 * @since 2025-01-15
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "敏感词", hidden = true)
public class SensitiveWordEntity extends BaseEntity {
  @Schema(description = "ID")
  private Long wordId;
  @Schema(description = "敏感词内容")
  private String wordContent;
  @Schema(description = "是否是黑名单(T/F)")
  private String isBlack;
}
