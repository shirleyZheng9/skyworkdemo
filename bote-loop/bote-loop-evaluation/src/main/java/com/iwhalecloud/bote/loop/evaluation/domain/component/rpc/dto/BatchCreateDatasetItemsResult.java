package com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto;

import com.iwhalecloud.bote.loop.evaluation.domain.entity.ItemErrorGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 批量创建数据集项目结果
 * 迁移对应关系: Go语言BatchCreateDatasetItems方法的返回值
 * - 功能: 批量创建数据集项目的结果对象
 * - 字段定义:
 * * idMap: Map<Long, Long> - ID映射关系
 * * errorGroup: List<ItemErrorGroup> - 错误组列表
 * <p>
 * Java实现说明:
 * - 对应Go的BatchCreateDatasetItems方法返回值
 * - 使用Lombok注解简化代码
 * - 支持Builder模式
 * <p>
 * 技术栈迁移:
 * - Go map[int64]int64 -> Java Map<Long, Long>
 * - Go []*entity.ItemErrorGroup -> Java List<ItemErrorGroup>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchCreateDatasetItemsResult {

  /**
   * ID映射关系
   * 迁移对应关系: Go语言BatchCreateDatasetItems返回的idMap
   */
  private Map<Long, Long> idMap;

  /**
   * 错误组列表
   * 迁移对应关系: Go语言BatchCreateDatasetItems返回的errorGroup
   */
  private List<ItemErrorGroup> errorGroup;
}
