package com.iwhalecloud.bote.dto.chat.export;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.iwhalecloud.bote.common.converter.LongTextIdConverter;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 会话消息
 *
 * @author qian.sisheng
 * @since 2025-11-11
 */
@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SessionSceneDTO {
  @ExcelProperty(value = "会话ID", converter = LongTextIdConverter.class)
  private Long sessionId;
  @ExcelProperty("会话标题")
  private String sessionTitle;
  @ExcelProperty("智能应用名称")
  private String botName;
  @ExcelProperty("智能体名称")
  private String sceneName;
  @ExcelProperty("创建人")
  private String creatorName;
  @ExcelProperty("创建时间")
  @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
  private Date createdTime;
  /** 场景ID */
  @ExcelIgnore
  private Long sceneId;
  /** 智能应用ID */
  @ExcelIgnore
  private Long botId;
  /** 智能应用租户ID */
  @ExcelIgnore
  private Long botTenantId;
  /** ID, 用于前端唯一值 */
  @ExcelIgnore
  private String id;
  /** 更新时间 */
  @ExcelIgnore
  private Date updatedTime;
  /** 更新人 */
  @ExcelIgnore
  private String updatorName;
}
