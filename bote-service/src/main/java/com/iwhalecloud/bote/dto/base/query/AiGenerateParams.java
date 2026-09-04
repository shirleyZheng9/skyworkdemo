package com.iwhalecloud.bote.dto.base.query;

import com.iwhalecloud.bote.dto.generator.flow.SkillBasicInfoDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * AI 生成参数
 *
 * @author chen.linfa
 * @since 2025-01-06
 */
@Getter
@Setter
@ToString
public class AiGenerateParams {
  @Schema(description = "租户 ID")
  private Long tenantId;
  @Schema(description = "请求")
  private String request;
  @Schema(description = "分类")
  private String type;
  @Schema(description = "工作流节点类型")
  private String nodeType;
  @Schema(description = "应用 ID")
  private Long sceneId;
  @Schema(description = "智能体名称")
  private String sceneName;
  @Schema(description = "智能体描述")
  private String sceneDesc;
  @Schema(description = "知识库 ID 列表")
  private List<Long> knowledgeIds;
  @Schema(description = "节点技能信息列表（包含技能编码和技能名称）")
  private List<SkillBasicInfoDTO> skills;
  @Schema(description = "是否支持视觉")
  private Boolean supportVision;
}
