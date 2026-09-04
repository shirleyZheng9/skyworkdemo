package com.iwhalecloud.bote.loop.client.data.dataset.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.base.BaseResp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 签名上传文件令牌响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignUploadFileTokenResponse {

  @JsonProperty("upload_url")
  private String uploadUrl;

  @JsonProperty("file_id")
  private String fileId;

  @JsonProperty("expires_in")
  private Integer expiresIn;

  @JsonProperty("BaseResp")
  private BaseResp baseResp;
}
