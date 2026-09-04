package com.iwhalecloud.bote.dto.knowledge.access.platform.request;

import com.iwhalecloud.bote.dto.knowledge.access.platform.DocResourceDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * 知识问答查询参数
 *
 * @author lxs
 * @since 2025/7/12
 */
@Data
public class KnowledgeCompleteRequest {

  @Schema(description = "文档资源列表")
  private List<DocResourceDTO> docResourceList;

  @Schema(description = "消息内容")
  private List<Message> messages;

  @Schema(description = "是否流式")
  private Boolean stream;

  @Data
  @Builder
  public static class Message {
      @Schema(description = "消息内容列表")
      private List<Content> content;
      @Schema(description = "角色")
      private String role;
  }

  @Data
  @Builder
  public static class Content {
      @Schema(description = "内容类型")
      private String type;
      @Schema(description = "文本内容")
      private String text;
  }

}
