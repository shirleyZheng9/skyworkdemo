package com.iwhalecloud.bote.dto.wechat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.util.Date;

/**
 * 微信AccessToken缓存DTO
 *
 * @author lizuyin
 * @since 2025/08/14
 */
@Getter
@Setter
@ToString
public class WechatAccessTokenDTO {

  /** accessToken */
  @Schema(description = "微信访问令牌")
  private String accessToken;
  /** 更新时间 */
  @Schema(description = "更新时间")
  private Date updateTime;
}


