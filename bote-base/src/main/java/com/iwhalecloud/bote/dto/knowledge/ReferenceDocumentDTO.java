package com.iwhalecloud.bote.dto.knowledge;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.iwhalecloud.bote.dto.beyond.BeyondReferenceDocumentDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 参考文档
 *
 * @author bianjp
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
@Schema(description = "参考文档")
public class ReferenceDocumentDTO {
  @Schema(description = "文档 ID")
  private String id;
  @Schema(description = "文档类型", allowableValues = "doc, image")
  private String type;
  @Schema(description = "文档名称/图片名称")
  private String name;
  @Schema(description = "图片路径")
  private String path;
  @Schema(description = "查看链接（后端仅在特殊情况下返回，一般由前端动态构造）")
  private String url;
  @Schema(description = "文档块列表")
  private List<ReferenceChunkDTO> chunks;
  @Schema(description = "是否开启下载")
  private Boolean downloadEnabled = true;
  @Schema(description = "是否允许打开文档")
  private Boolean openEnabled = true;

  // 用于百应调用我们的智能体执行接口时，我们能把百应知识库返回的参考文档原样返回给百应
  @Schema(description = "百应参考文档")
  private BeyondReferenceDocumentDTO beyondDocument;
}
