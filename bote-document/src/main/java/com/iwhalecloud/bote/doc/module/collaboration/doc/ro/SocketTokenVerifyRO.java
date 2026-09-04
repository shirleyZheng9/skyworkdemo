package com.iwhalecloud.bote.doc.module.collaboration.doc.ro;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * socket连接的token验证请求参数
 *
 * @author Aiqing
 * @since 2025/8/30
 */
@Getter
@Setter
@ToString
public class SocketTokenVerifyRO {

  @Schema(description = "token")
  private String token;
}
