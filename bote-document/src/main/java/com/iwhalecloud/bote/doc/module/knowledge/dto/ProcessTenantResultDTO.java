package com.iwhalecloud.bote.doc.module.knowledge.dto;

import lombok.Getter;

import java.util.List;
import lombok.Setter;
import lombok.ToString;

/**
 * 处理租户文件的结果
 *
 * @author auto
 * @since 2025-12-24
 */
@Getter
@Setter
@ToString(callSuper = true)
public class ProcessTenantResultDTO {
  /**
   * 成功数
   */
  private  int successCount;
  /**
   * 涉及到的文件信息id
   */
  private  List<String> processedFileInfoIds;

  public ProcessTenantResultDTO(int successCount, List<String> processedFileInfoIds) {
    this.successCount = successCount;
    this.processedFileInfoIds = processedFileInfoIds;
  }
}

