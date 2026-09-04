package com.iwhalecloud.bote.doc.module.knowledge.dto.query;

import com.iwhalecloud.bote.doc.common.model.PageParams;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 知识库运营查询参数
 *
 * @author qian.sisheng
 * @since 2026/02/27
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "知识库运营查询参数")
public class KnowledgeOpsQueryParams extends PageParams {

  @Schema(description = "时间维度：day/week/month/year")
  private String timeType;
  @Schema(description = "开始时间")
  private Date startTime;
  @Schema(description = "结束时间")
  private Date endTime;
  @Schema(description = "搜索内容")
  private String searchContent;
  @Schema(description = "反馈类型列表", hidden = true)
  private List<String> feedbackTypes;
}
