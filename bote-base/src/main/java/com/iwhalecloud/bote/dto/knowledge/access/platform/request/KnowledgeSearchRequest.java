package com.iwhalecloud.bote.dto.knowledge.access.platform.request;

import com.iwhalecloud.bote.dto.knowledge.access.platform.DocResourceDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 知识召回查询参数
 *
 * @author lxs
 * @since 2025/7/12
 */
@Getter
@Setter
@ToString(callSuper = true)
public class KnowledgeSearchRequest {

  @Schema(description = "检索内容")
  private String query;

  @Schema(description = "文档资源列表")
  private List<DocResourceDTO> docResourceList;

  @Schema(description = "返回数量，联网搜索时最大单次50")
  private Integer size;

  @Schema(description = "是否关联返回上下文")
  private Boolean withContext;

  @Schema(description = "分数阈值,示例值(0.1)")
  private Float score;

}
