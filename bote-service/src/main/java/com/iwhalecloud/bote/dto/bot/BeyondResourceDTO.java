package com.iwhalecloud.bote.dto.bot;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 百应平台资源DTO（智能应用与智能体混合）
 *
 * @author lizuyin
 * @since 2025-11-27
 */
@Getter
@Setter
@ToString
@Schema(description = "百应平台资源DTO")
public class BeyondResourceDTO {
  @Schema(description = "资源ID")
  private Long resourceId;
  @Schema(description = "资源名称")
  private String resourceName;
  @Schema(description = "资源描述")
  private String resourceDesc;
  @Schema(description = "资源图标")
  private String avatar;
  @Schema(description = "资源状态")
  private String resourceStatus;
  @Schema(description = "上下架状态 0-未上架 1-已上架 2-已下架")
  private String status;
  @Schema(description = "租户ID")
  private Long tenantId;
  @Schema(description = "资源类型")
  private String resourceType;
  @Schema(description = "授权状态")
  private String authStatus;
  @Schema(description = "更新时间")
  private Date updateTime;
  @Schema(description = "资源访问URL的相对路径")
  private String detailUrl;
  @Schema(description = "场景类型（仅AGENT类型有值）")
  private String sceneType;
  @Schema(description = "资源创建人姓名")
  private String createUserName;
  @Schema(description = "创建时间")
  private Date createTime;
}

