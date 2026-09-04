package com.iwhalecloud.bote.doc.module.person.dto.share;

import java.util.List;
import lombok.Data;

/**
 * 聚合共享信息 DTO
 * <p>用于聚合文档的共享信息，包括基础信息、共享目标列表和最高权限</p>
 *
 * @author lizuyin
 * @since 2025-08-20
 */
@Data
public class AggregatedShareDTO {

  /** 基础共享信息 */
  private MyShareDTO dto;
  /** 共享目标列表 */
  private List<SharedTargetDTO> sharedTo;
  /** 最高权限 */
  private String highestPermission;

  /**
   * 构造函数
   *
   * @param base 基础共享信息
   */
  public AggregatedShareDTO(MyShareDTO base) {
    this.dto = base;
    this.sharedTo = new java.util.ArrayList<>();
  }
}

