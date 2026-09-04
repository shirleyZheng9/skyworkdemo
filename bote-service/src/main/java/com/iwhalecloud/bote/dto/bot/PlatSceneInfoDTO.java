package com.iwhalecloud.bote.dto.bot;

import com.iwhalecloud.bote.dto.base.LabelObjectRelDTO;
import com.iwhalecloud.bote.entity.bot.PlatSceneInfoEntity;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 模板智能体 DTO
 *
 * @author auto
 * @since 2025-06-21
 */
@Getter
@Setter
@ToString(callSuper = true)
public class PlatSceneInfoDTO extends PlatSceneInfoEntity {

  @Schema(description = "场景名称")
  private String sceneName;

  @Schema(description = "场景描述")
  private String sceneDesc;

  @Schema(description = "场景图标")
  private String sceneIcon;

  @Schema(description = "场景状态")
  private String sceneStatus;

  @Schema(description = "场景分类名称")
  private String catalogName;

  @Schema(description = "操作人名称")
  private String updatorName;

  @Schema(description = "创建人名称")
  private String creatorName;

  @DiffField(childNode = true)
  @Schema(description = "关联标签，后端保存数据库时使用，前端展示也需要")
  private List<LabelObjectRelDTO> labels;

  @Schema(description = "智能体类型")
  private String sceneType;
}
