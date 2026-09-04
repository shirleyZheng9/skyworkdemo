package com.iwhalecloud.bote.doc.module.control.model;

import lombok.Data;

/**
 * 节点基础信息DTO
 *
 * <p>用于传输文档节点的基本信息，包含节点的标识、名称、父子关系等核心属性。</p>
 *
 * @author Aiqing
 * @since 2025-08-21
 */
@Data
public class NodeBaseInfoDTO {

  /**
   * 文档库ID
   *
   */
  private String libraryId;

  /**
   * 节点ID
   */
  private String nodeId;

  /**
   * 节点名称
   *
   */
  private String nodeName;

  /**
   * 节点图标
   *
   */
  private String icon;

  /**
   * 父节点ID
   *
   * <p>根节点的父节点ID为null</p>
   */
  private String parentId;

  /**
   * 创建人
   */
  private Long creatorId;

  /**
   * 节点类型
   *
   * @see com.iwhalecloud.bote.doc.consts.DocumentTypeEnum
   */
  private String nodeType;

  /**
   * 内容来源
   */
  private String contentSource;

  /**
   * 权限模式
   */
  private Integer permissionMode;
}
