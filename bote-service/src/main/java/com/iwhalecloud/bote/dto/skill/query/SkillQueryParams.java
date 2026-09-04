package com.iwhalecloud.bote.dto.skill.query;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.dto.base.query.PagingQueryParams;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 技能查询参数
 *
 * @author auto
 * @since 2024-09-15
 */
@Getter
@Setter
@ToString(callSuper = true)
public class SkillQueryParams extends PagingQueryParams {
  @Schema(description = "模糊查询")
  private String searchContent;
  @Schema(description = "流程类型")
  private String flowType;
  @Schema(description = "租户ID")
  private Long tenantId;
  @Schema(description = "目录ID")
  private Long catalogItemId;
  @Schema(description = "流程ID")
  private Long flowId;
  @Schema(description = "目录ID列表", hidden = true)
  private List<Long> catalogItemList;
  @Schema(description = "是否用于配置管理", example = "T/F")
  private String configFlag;
  @Schema(description = "属性ID")
  private Long attrId;
  @Schema(description = "来源：平台:platform 租户:tenant")
  private String sourceFrom;
  @Schema(description = "模型ID")
  private Long modelId;
  @Schema(description = "提示词ID列表", hidden = true)
  private List<Long> promptIds;
  @Schema(description = "页面来源类型")
  private String pageSourceType;
  @Schema(description = "终端类型")
  private String terminalType;
  @Schema(description = "脚本类型", allowableValues = {BaseConsts.SCRIPT_TYPE_GROOVY, BaseConsts.SCRIPT_TYPE_PYTHON3})
  private String scriptType;
  @Schema(description = "技能 ID", hidden = true)
  private Long skillId;
  @Schema(description = "是否mock, T/F")
  private String isMock;
  @Schema(description = "数据库渠道来源")
  private String dataSourceChannel;
  @Schema(description = "是否用于安全围栏, T/F")
  private String isSecurity;
}
