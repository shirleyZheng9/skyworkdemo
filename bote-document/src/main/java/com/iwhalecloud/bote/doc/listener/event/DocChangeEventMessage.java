package com.iwhalecloud.bote.doc.listener.event;

import com.iwhalecloud.bote.doc.consts.DocumentActionTypeEnum;
import com.iwhalecloud.bss.litchi.disruptor.DisruptorObject;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文档库文档更新事件消息
 *
 * @author Aiqing
 * @since 2025/8/19
 */
@Getter
@Setter
@ToString
public class DocChangeEventMessage implements DisruptorObject {

  /** 新增、更新、删除 */
  private DocumentActionTypeEnum changeType;
  /** 文档库ID */
  private String libraryId;
  /** 文档ID */
  private String documentId;
  /** 文档名称 */
  private String documentName;
  /** 文档库ID */
  private String targetLibraryId;
  /** 更新人 */
  private Long updatorId;
  /** 更新时间 */
  private Date updatedTime;
  /** 请求用户客户端IP */
  private String clientIp;
  /** 租户ID */
  private Long tenantId;
  /** 企业空间 ID */
  private Long spaceId;
  /** 编辑模式 CORRECTION修订或者 其他的都是编辑*/
  private String mode;

  /** 是否为在线文档编辑，在线文档不会里面同步构建知识库需要发布 */
  private String onLineEditing;

}
