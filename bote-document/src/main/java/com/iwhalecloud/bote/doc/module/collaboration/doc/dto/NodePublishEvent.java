package com.iwhalecloud.bote.doc.module.collaboration.doc.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * nodejs协作服务广播消息内容
 *
 * @author Aiqing
 * @since 2025/8/26
 */
@Getter
@Setter
@ToString
public class NodePublishEvent {

  @Schema(description = "文档ID")
  private String documentId;
  @Schema(description = "消息内容")
  private String message;
  @Schema(description = "消息推送方，hocuspocus客户端标识")
  private String publisher;
}
