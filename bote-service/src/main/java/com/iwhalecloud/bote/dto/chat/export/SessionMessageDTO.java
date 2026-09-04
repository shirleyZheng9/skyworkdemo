package com.iwhalecloud.bote.dto.chat.export;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.iwhalecloud.bote.common.converter.LongTextIdConverter;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 会话详情数据
 *
 * @author qian.sisheng
 * @since 2025-11-11
 */
@Getter
@Setter
@ToString
public class SessionMessageDTO {
  @ExcelProperty(value = "会话ID", converter = LongTextIdConverter.class)
  private Long sessionId;
  @ExcelProperty("会话标题")
  private String sessionTitle;
  @ExcelProperty(value = "会话详情ID", converter = LongTextIdConverter.class)
  private Long msgId;
  @ExcelProperty("会话内容")
  private String msgText;
  @ExcelProperty("智能应用名称")
  private String botName;
  @ExcelProperty("智能体名称")
  private String sceneName;
  @ExcelProperty("点踩状态")
  private String likeType;
  @ExcelProperty("点踩反馈信息")
  private String feedback;
  @ExcelProperty("创建人")
  private String creatorName;
  @ExcelProperty("创建时间")
  @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
  private Date createdTime;
}
