package com.iwhalecloud.bote.dto.skill;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.base.SimpleFlowStepDTO;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphDTO;
import com.iwhalecloud.bote.entity.skill.SkillFlowEntity;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

/**
 * 技能：流程 DTO
 *
 * @author auto
 * @since 2024-09-18
 */
@Getter
@Setter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
public class SkillFlowDTO extends SkillFlowEntity {
  @Schema(description = "修改人名称")
  private String updatorName;
  @Schema(description = "创建人名称")
  private String creatorName;
  @DiffField(childNode = true)
  @Schema(description = "流程参数", hidden = true)
  private SkillFlowParamDTO param;
  @Schema(description = "流程图")
  @SuppressFBWarnings("SE_BAD_FIELD")
  private SceneGraphDTO graph;
  @Schema(description = "变量列表")
  @SuppressFBWarnings("SE_BAD_FIELD")
  private List<ParameterSpec> variables;
  @Schema(description = "入参")
  @SuppressFBWarnings("SE_BAD_FIELD")
  private ParameterSpec request;
  @Schema(description = "出参")
  @SuppressFBWarnings("SE_BAD_FIELD")
  private ParameterSpec response;
  @Schema(description = "复制的流程 ID")
  private Long copyFlowId;
  @Schema(description = "步骤信息")
  private List<SimpleFlowStepDTO> flowSteps;

  /**
   * 解析流程图
   */
  public void parseGraph() {
    if (StringUtils.isNotEmpty(getFlowGraphJson())) {
      graph = JsonUtil.parseJsonRequired(getFlowGraphJson(), SceneGraphDTO.class);
    }
    setFlowGraphJson(null);
  }

  /**
   * 解析参数
   */
  public void parseParams() {
    if (param != null) {
      if (StringUtils.isNotEmpty(param.getVariableJson())) {
        variables = JsonUtil.parseJsonRequired(param.getVariableJson(), new TypeReference<List<ParameterSpec>>() {
        });
      }
      if (StringUtils.isNotEmpty(param.getRequestJson())) {
        request = JsonUtil.parseJsonRequired(param.getRequestJson(), ParameterSpec.class);
      }
      if (StringUtils.isNotEmpty(param.getResponseJson())) {
        response = JsonUtil.parseJsonRequired(param.getResponseJson(), ParameterSpec.class);
      }
    }
    param = null;
  }
}
