package com.iwhalecloud.bote.doc.module.knowledge.entity;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * WeKnora 会话映射表 Entity
 *
 * <p>记录博特会话与 WeKnora session_id 的映射关系，避免重复创建 Session。
 * 对应表字段 {@code creator_id}。</p>
 *
 * @author bianjp
 * @since 2026-03-31
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_weknora_chat_session")
public class WeKnoraChatSessionEntity extends BaseEntity {

  @DiffId
  @Schema(description = "主键")
  private Long weKnoraChatSessionId;

  @DiffField(name = "BOTE_SESSION_ID")
  @Schema(description = "博特会话/请求标识（clientId 或自定义 key）")
  private String boteSessionId;

  @DiffField(name = "WEKNORA_SESSION_ID")
  @Schema(description = "WeKnora 侧 session_id")
  private String weKnoraSessionId;

  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户 ID")
  private Long tenantId;

  @DiffField(name = "CREATOR_ID")
  @Schema(description = "创建人 ID，对应列 creator_id")
  private Long creatorId;

  @DiffField(name = "CREATE_TIME")
  @Schema(description = "创建时间")
  private Date createTime;
}
