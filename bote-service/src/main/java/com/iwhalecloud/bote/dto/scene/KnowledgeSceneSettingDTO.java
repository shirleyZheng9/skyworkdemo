package com.iwhalecloud.bote.dto.scene;

import com.iwhalecloud.bote.common.consts.ThinkingStrategy;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 知识问答场景设置
 *
 * @author bianjp
 * @since 2025-03-13
 */
@Getter
@Setter
@ToString
@Schema(description = "知识问答场景设置")
public class KnowledgeSceneSettingDTO {
  /** 默认设置 */
  public static final KnowledgeSceneSettingDTO DEFAULT = new KnowledgeSceneSettingDTO();

  @Schema(description = "是否开启参考文档")
  private Boolean referencesEnabled;
  @Schema(description = "是否开启追问")
  private Boolean questionsEnabled;
  @Schema(description = "是否开启上下文")
  private Boolean chatLogEnabled;
  @Schema(description = "推理策略")
  private ThinkingStrategy thinkingStrategy;
}
