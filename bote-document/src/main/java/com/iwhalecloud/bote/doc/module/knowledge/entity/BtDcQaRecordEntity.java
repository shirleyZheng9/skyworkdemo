package com.iwhalecloud.bote.doc.module.knowledge.entity;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 问答记录表 Entity
 *
 * @author linmengfan
 * @since 2025-09-13
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "BT_DC_QA_RECORD")
public class BtDcQaRecordEntity extends BaseEntity {

  @DiffId
  @Schema(description = "主键")
  private Long qaId;
  @DiffField(name = "SESSION_ID")
  @Schema(description = "会话ID")
  private String sessionId;
  @DiffField(name = "CHAT_LOG_ID")
  @Schema(description = "对话记录ID（三方）")
  private String chatLogId;
  @DiffField(name = "QUESTION")
  @Schema(description = "问题内容")
  private String question;
  @DiffField(name = "STANDARD_QUESTION")
  @Schema(description = "语义聚合标准问法（高频统计归并）")
  private String standardQuestion;
  @DiffField(name = "ANSWER")
  @Schema(description = "回答内容")
  private String answer;
  @DiffField(name = "CONFIDENCE_SCORE")
  @Schema(description = "整体置信度分数")
  private BigDecimal confidenceScore;
  @DiffField(name = "RESPONSE_TIME")
  @Schema(description = "响应时间（毫秒）")
  private Long responseTime;
  @DiffField(name = "CHUNK_COUNT")
  @Schema(description = "引用片段数量")
  private Long chunkCount;
  @DiffField(name = "USER_FEEDBACK")
  @Schema(description = "用户反馈：LIKE-点赞，DISLIKE-点踩")
  private String userFeedback;
  @DiffField(name = "FEEDBACK_REASON")
  @Schema(description = "反馈原因")
  private String feedbackReason;
  @DiffField(name = "FEEDBACK_TYPE")
  @Schema(description = "反馈类型")
  private String feedbackType;
  @DiffField(name = "OPERATE_STATE")
  @Schema(description = "操作状态:0-未处理,1-已处理")
  private String operateState;
  @DiffField(name = "FEEDBACK_TIME")
  @Schema(description = "反馈时间")
  private Date feedbackTime;
  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户 ID")
  private Long tenantId;
  @DiffField(name = "BOT_ID")
  @Schema(description = "机器人 ID")
  private Long botId;
}
