package com.iwhalecloud.bote.doc.module.knowledge.dto.query;

import java.util.Date;
import java.util.List;

import com.iwhalecloud.bote.doc.common.model.TenantBaseRO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 看板信息
 *
 * @author linmengfan
 * @since 2025-09-13
 */
@Getter
@Setter
@ToString(callSuper = true)
public class BtDcQaRecordNoticeBoardQueryParams extends TenantBaseRO {
  @Schema(description = "知识库id列表")
  private List<Long> knowledgeIds;

  @Schema(description = "查看统计维度：1D-今天，3D-三天，1W-一周，1M-一个月，1Y-一年")
  private String timeType;

  @Schema(description = "开始时间")
  private Date startTime;

  @Schema(description = "结束时间")
  private Date endTime;

  @Schema(description = "查看统计维度：hitRate-问答命中率，qaCount-问答次数，qaPeoples-问答人数")
  private String cartType;

  @Schema(description = "登陆人id")
  private Long userId;
  @Schema(description = "部门ID")
  private List<Long> deptIds;
  @Schema(description = "是否超级管理员")
  private Boolean superAdmin;
}
