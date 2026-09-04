package com.iwhalecloud.bote.loop.client.data.dataset.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.base.Base;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 签名上传文件令牌请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignUploadFileTokenRequest {

  @JsonProperty("workspace_id")
  private Long workspaceId;

  @JsonProperty("file_name")
  private String fileName;

  @JsonProperty("file_size")
  private Long fileSize;

  @JsonProperty("content_type")
  private String contentType;

  @JsonProperty("Base")
  private Base base;
}
