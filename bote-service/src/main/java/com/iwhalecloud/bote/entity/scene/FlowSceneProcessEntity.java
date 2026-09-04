package com.iwhalecloud.bote.entity.scene;

import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 智能体执行进度
 *
 * @author chen.linfa
 * @since 2025-07-21
 */
@Getter
@Setter
@ToString(callSuper = true)
@Table(name = "bt_flow_scene_process")
public class FlowSceneProcessEntity extends BaseEntity {
  /** 主键 */
  private Long id;
  /** 租户 ID */
  private Long tenantId;
  /** 当前智能体标识 */
  private Long sceneId;
  /** 当前智能体会话标识 */
  private String contextId;
  /** 当前智能体在主流程中的节点编码 */
  private String nodeCode;
  /** 进度 */
  private String flowStatus;

  /** 主智能体标识 */
  private Long mainSceneId;
  /** 主智能体上下文 ID */
  private String mainContextId;
  /** 主智能体会话标识 */
  private Long mainConversationId;
}
