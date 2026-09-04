package com.iwhalecloud.bote.doc.module.knowledge.dto;

import com.iwhalecloud.bote.dto.base.CatalogDTO;
import lombok.Getter;

import java.util.Map;
import lombok.Setter;
import lombok.ToString;

/**
 * 目录结构信息
 *
 * @author auto
 * @since 2025-12-24
 */
@Getter
@Setter
@ToString(callSuper = true)
public class CatalogStructureDTO {
  /**
   * 目录id对应的目录名和路径
   */
  private  Map<Long, Map<String, String>> catalogIdMap;

  /**
   * 目录id和目录的映射
   */
  private  Map<Long, CatalogDTO> catalogMap;

  public CatalogStructureDTO(Map<Long, Map<String, String>> catalogIdMap, Map<Long, CatalogDTO> catalogMap) {
    this.catalogIdMap = catalogIdMap;
    this.catalogMap = catalogMap;
  }
}

