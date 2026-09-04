package com.iwhalecloud.bote.dto.beyond;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * 百应资源发布请求对象
 *
 * @author lizuyin
 * @since 2025-07-21
 */
@Getter
@Setter
@ToString
public class BeyondResourcePublishRequest {
  /** 发布渠道列表 */
  private List<PublishChannelDTO> publishChannels;
  /** 资源列表 */
  private List<ResourceDTO> resources;
  /** 发布类型，publish:公开，private:私有，默认publish */
  private String publishType;
}
