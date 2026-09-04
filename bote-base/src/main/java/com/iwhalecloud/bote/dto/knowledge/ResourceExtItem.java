package com.iwhalecloud.bote.dto.knowledge;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author 赵旭
 * @since 2025/7/23 21:14
 */
@Getter
@Setter
@ToString
public class ResourceExtItem {
  /** 文档ID,用于问答和问答接口 */
  private String resourceWid;
  /** RESOURCE:个人资源、KNOW_BASE_RESOURCE:知识库资源 */
  private String resourceType;
  /** 记录文档名称,用于流程节点回显 */
  private String docName;
}
