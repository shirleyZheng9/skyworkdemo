package com.iwhalecloud.bote.dto.publish;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.iwhalecloud.bote.dto.beyond.PublishChannelDTO;
import com.iwhalecloud.bote.entity.publish.ResourcePublishRecordEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 资源发布记录 DTO
 *
 * @author lizuyin
 * @since 2025-07-25
 */
@Getter
@Setter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
public class ResourcePublishRecordDTO extends ResourcePublishRecordEntity {
  @Schema(description = "资源名称")
  private String resourceName;
  @Schema(description = "创建人姓名")
  private String creatorName;
  @Schema(description = "更新人姓名")
  private String updatorName;
  @Schema(description = "发布渠道描述")
  private String publishChannelDesc;
  @Schema(description = "发布渠道配置")
  private PublishChannelDTO publishChannelConfig;
  @Schema(description = "备注")
  private String remark;
  @Schema(description = "回调地址")
  private String callbackUrl;
  @Schema(description = "发布名称")
  private String name;
  @Schema(description = "发布图标Url")
  private String avatarUrl;
  @Schema(description = "应用归属的空间ID，用于 boteclaw")
  private Long botTenantId;
}
