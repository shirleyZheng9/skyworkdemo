package com.iwhalecloud.bote.dto.wechat;

import com.iwhalecloud.bote.dto.publish.ResourcePublishRecordDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

/**
 * 微信配置中转DTO
 *
 * @author lizuyin
 * @since 2025-08-14
 */
@Getter
@Setter
@ToString
public class WechatCfgDTO {

  /** 配置Map */
  private Map<String, Object> cfgMap;
  /** 发布记录 */
  private ResourcePublishRecordDTO record;

}
