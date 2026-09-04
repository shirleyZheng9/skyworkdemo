package com.iwhalecloud.bote.loop.client.data.domain.dataset;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件上传令牌数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileUploadTokenDTO {

  @JsonProperty("access_key_id")
  private String accessKeyId;

  @JsonProperty("secret_access_key")
  private String secretAccessKey;

  @JsonProperty("session_token")
  private String sessionToken;

  @JsonProperty("expired_time")
  private String expiredTime;

  @JsonProperty("current_time")
  private String currentTime;
}
