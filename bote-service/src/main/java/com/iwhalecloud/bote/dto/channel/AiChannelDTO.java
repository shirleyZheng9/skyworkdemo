package com.iwhalecloud.bote.dto.channel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.iwhalecloud.bote.entity.channel.AiChannelEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

/**
 * 渠道配置DTO
 *
 * @author wangtingyun
 * @since 2026-03-09
 */
@Getter
@Setter
@ToString(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AiChannelDTO extends AiChannelEntity {

  @Schema(description = "渠道名称")
  private String channelName;
  @Schema(description = "帮助文档地址")
  private String channelDocUrl;
  @Schema(description = "渠道对象列表")
  private List<Map<String, Object>> channelObjList;

}
