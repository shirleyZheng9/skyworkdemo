package com.iwhalecloud.bote.dto.portal;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 钉钉对接客户端参数
 *
 * @author Aiqing
 * @since 2025/6/5
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class DingTalkParameterDTO {

  @Schema(description = "钉钉企业ID")
  private String corpId;
  @Schema(description = "钉钉认证登录URL")
  private String oauthUrl;
}
