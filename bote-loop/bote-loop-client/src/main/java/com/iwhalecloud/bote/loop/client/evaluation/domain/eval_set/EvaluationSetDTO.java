package com.iwhalecloud.bote.loop.client.evaluation.domain.eval_set;

import com.iwhalecloud.bote.loop.client.common.userinfo.UserInfoCarrier;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.DatasetFeaturesDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.DatasetSpecDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.DatasetStatusDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.common.BaseInfoDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评测集数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "评测集")
public class EvaluationSetDTO implements UserInfoCarrier {

  @Schema(description = "ID")
  private Long id;

  @Schema(description = "应用ID")
  private Integer appId;

  @Schema(description = "工作空间ID")
  private Long workspaceId;

  @Schema(description = "名称")
  private String name;

  @Schema(description = "描述")
  private String description;

  @Schema(description = "状态")
  private DatasetStatusDTO status;

  @Schema(description = "规格限制")
  private DatasetSpecDTO spec;

  @Schema(description = "功能开关")
  private DatasetFeaturesDTO features;

  @Schema(description = "数据条数")
  private Long itemCount;

  @Schema(description = "是否有未提交的修改")
  private Boolean changeUncommitted;

  @Schema(description = "业务分类")
  private BizCategoryDTO bizCategory;

  @Schema(description = "版本信息")
  private EvaluationSetVersionDTO evaluationSetVersion;

  @Schema(description = "最新的版本号")
  private String latestVersion;

  @Schema(description = "下一个的版本号")
  private Long nextVersionNum;

  @Schema(description = "系统信息")
  private BaseInfoDTO baseInfo;

  @Schema(description = "目录 ID")
  private Long catalogItemId;
}
