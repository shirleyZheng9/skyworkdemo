package com.iwhalecloud.bote.dto.publish;

import com.iwhalecloud.bote.common.enums.BeyondResourceBizTypeEnum;
import com.iwhalecloud.bote.common.enums.PublishChannelEnum;
import com.iwhalecloud.bote.dto.beyond.PublishChannelDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 资源发布请求DTO
 *
 * @author lizuyin
 * @since 2025-07-24
 */
@Getter
@Setter
@ToString
@Schema(description = "资源发布请求")
public class ResourcePublishRequest {

  @NotNull(message = "租户ID不能为空")
  @Schema(description = "租户ID")
  private Long tenantId;

  @NotNull(message = "资源类型不能为空")
  @Schema(description = "资源类型")
  private BeyondResourceBizTypeEnum resourceType;

  @NotEmpty(message = "资源ID列表不能为空")
  @Schema(description = "资源ID列表")
  private List<Long> resourceIdList;

  @NotNull(message = "发布渠道不能为空")
  @Schema(description = "发布渠道")
  private PublishChannelEnum publishChannel;

  @NotNull(message = "发布渠道配置不能为空")
  @Schema(description = "发布渠道配置")
  private PublishChannelDTO publishChannelConfig;

  @Schema(description = "发布Id")
  private Long recordId;

  /** 发布类型，publish:公开，private:私有，默认publish */
  @Schema(description = "发布类型，publish:公开，private:私有，默认publish ")
  private String publishType;
}
