package com.iwhalecloud.bote.dto.base;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * 账号事件日志实体
 *
 * @author tingyun.wang
 * @since 2025-07-15
 */
@Setter
@Getter
@ToString
public class AccountEventLog {

  @Schema(description = "日志主键")
  private Long logId;
  @Schema(description = "操作人ID")
  private Long operatorId;
  @Schema(description = "操作人姓名")
  private String operatorName;
  @Schema(description = "事件类型")
  private String eventType;
  @Schema(description = "事件编码")
  private String eventCode;
  @Schema(description = "事件描述")
  private String eventDesc;
  @Schema(description = "事件内容信息")
  private String eventContent;
  @Schema(description = "操作IP")
  private String operIp;
  @Schema(description = "操作时间")
  private Date operDate;
  @Schema(description = "租户ID")
  private Long tenantId;
  @Schema(description = "备注")
  private String remark;

}
