package com.iwhalecloud.bote.dto.bot.query;

import com.iwhalecloud.bote.dto.base.query.PagingQueryParams;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 场景查询参数
 *
 * @author chen.linfa
 * @since 2024-08-02
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "场景查询参数")
public class BotSceneQueryParams extends PagingQueryParams {
  @Schema(description = "名称/编码模糊查询")
  private String searchContent;
  @Schema(description = "租户 ID")
  private Long tenantId;
  @Schema(description = "机器人 ID")
  private Long botId;
  @Schema(description = "场景状态: 0:下架，1:上架")
  private String sceneStatus;
  @Schema(description = "目录 ID")
  private Long catalogItemId;
  @Schema(description = "目录 ID 列表", hidden = true)
  private List<Long> catalogItemList;
  @Schema(description = "例外的标签")
  private List<String> excludeLabels;

  @Schema(description = "场景类型集合")
  private List<String> sceneTypes;
  @Schema(description = "场景状态集合")
  private List<String> status;
  @Schema(description = "标签集合")
  private List<Long> labelIds;
  @Schema(description = "百应平台发布状态（S:成功，F:失败等）")
  private String beyondPublishStatus;
}
