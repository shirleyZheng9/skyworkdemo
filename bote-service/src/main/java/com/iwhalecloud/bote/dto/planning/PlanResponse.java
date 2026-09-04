package com.iwhalecloud.bote.dto.planning;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 执行计划出参
 *
 * @author chen.linfa
 * @since 2025-05-19
 */
@Getter
@Setter
@ToString
public class PlanResponse {

  @Schema(description = "是否成功")
  private Boolean success;
  @Schema(description = "耗时（毫秒）")
  private Long timeSpent;
  @Schema(description = "错误信息")
  private String failMsg;
  @Schema(description = "错误堆栈")
  private String failStack;

  public static PlanResponse success(Date startTime) {
    PlanResponse response = new PlanResponse();
    response.setSuccess(true);
    response.setTimeSpent(System.currentTimeMillis() - startTime.getTime());
    return response;
  }

  /**
   * 构造执行失败响应
   *
   * @param startTime 开始时间
   * @param failMsg 错误信息
   */
  public static PlanResponse fail(Date startTime, String failMsg) {
    PlanResponse response = new PlanResponse();
    response.setSuccess(false);
    response.setTimeSpent(System.currentTimeMillis() - startTime.getTime());
    response.setFailMsg(failMsg);
    return response;
  }

}
