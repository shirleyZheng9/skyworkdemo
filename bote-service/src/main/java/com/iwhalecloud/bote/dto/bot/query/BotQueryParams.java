package com.iwhalecloud.bote.dto.bot.query;

import com.iwhalecloud.bote.dto.base.query.PagingQueryParams;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 机器人查询参数
 *
 * @author auto
 * @since 2024-09-14
 */
@Getter
@Setter
@ToString(callSuper = true)
public class BotQueryParams extends PagingQueryParams {
  @Schema(description = "名称/编码模糊查询")
  private String searchContent;
  @Schema(description = "租户 ID")
  private Long tenantId;
  @Schema(description = "用户 ID")
  private Long userId;
  @Schema(description = "机器人 ID")
  private Long botId;
  @Schema(description = "机器人 ID 集合")
  private List<Long> botIds;
  @Schema(description = "机器人状态")
  private String botStatus;
  @Schema(description = "成员用户 ID")
  private Long memberId;
  @Schema(description = "是否发布")
  private Boolean published;
  @Schema(description = "策略类型")
  private String strategyType;
  @Schema(description = "是否为自由模式策略")
  private Boolean strategyTypeNone;
  @Schema(description = "数据来源")
  private String dataFrom;
  @Schema(description = "空间 ID")
  private Long spaceId;
  @Schema(description = "是否为平台管理员")
  private Boolean isAdmin;
  @Schema(description = "授权组织 ID 列表")
  private List<Long> orgIds;

  @Schema(description = "场景 ID 列表")
  private List<Long> sceneIds;
  @Schema(description = "场景状态")
  private String sceneStatus;
  @Schema(description = "场景状态集合")
  private List<String> status;
  @Schema(description = "场景类型")
  private String sceneType;
  @Schema(description = "例外的标签")
  private String excludeLabel;
  @Schema(description = "目录 ID")
  private Long catalogItemId;
  @Schema(description = "目录 ID 列表", hidden = true)
  private List<Long> catalogItemList;
  @Schema(description = "百应平台发布状态（S:成功，F:失败等）")
  private String beyondPublishStatus;
}
