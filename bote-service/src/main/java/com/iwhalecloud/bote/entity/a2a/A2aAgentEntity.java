package com.iwhalecloud.bote.entity.a2a;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * A2A 服务
 *
 * @author bianjp
 * @since 2025-09-08
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "A2A 服务")
@DiffNode(name = "bt_a2a_agent")
public class A2aAgentEntity extends BaseEntity {
  @Schema(description = "智能体 ID")
  @DiffId
  protected Long agentId;
  @Schema(description = "智能体名称")
  @DiffField
  protected String agentName;
  @Schema(description = "智能体描述")
  @DiffField
  protected String agentDesc;
  @Schema(description = "智能体卡片地址")
  @DiffField
  protected String agentCardUrl;
  @Schema(description = "智能体卡片 JSON")
  @DiffField
  protected String agentCardJson;
  @Schema(description = "鉴权配置 JSON")
  @DiffField
  protected String authConfigJson;
  @Schema(description = "技能数量(管理列表页要显示，冗余存储以避免查询智能体卡片)")
  @DiffField
  protected Integer skillCount;
  @Schema(description = "目录 ID")
  @DiffField
  protected Long catalogItemId;
  @Schema(description = "租户 ID")
  @DiffField
  protected Long tenantId;
  @Schema(description = "平台 ID")
  @DiffField
  protected Long platformId;
  @Schema(description = "外系统智能体 ID")
  @DiffField
  protected String extAgentId;
}
