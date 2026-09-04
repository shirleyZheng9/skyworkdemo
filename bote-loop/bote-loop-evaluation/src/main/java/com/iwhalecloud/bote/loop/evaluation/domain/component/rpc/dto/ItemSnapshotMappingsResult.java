package com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto;

import com.iwhalecloud.bote.loop.evaluation.domain.entity.ItemSnapshotFieldMapping;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 项目快照映射结果
 * 迁移对应关系: Go语言QueryItemSnapshotMappings方法的返回值
 * - 功能: 查询项目快照映射的结果对象
 * - 字段定义:
 * * fieldMappings: List<ItemSnapshotFieldMapping> - 字段映射列表
 * * syncCkDate: String - 同步CK日期
 * <p>
 * Java实现说明:
 * - 对应Go的QueryItemSnapshotMappings方法返回值
 * - 使用Lombok注解简化代码
 * - 支持Builder模式
 * <p>
 * 技术栈迁移:
 * - Go []*entity.ItemSnapshotFieldMapping -> Java List<ItemSnapshotFieldMapping>
 * - Go string -> Java String
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemSnapshotMappingsResult {

  /**
   * 字段映射列表
   * 迁移对应关系: Go语言QueryItemSnapshotMappings返回的fieldMappings
   */
  private List<ItemSnapshotFieldMapping> fieldMappings;

  /**
   * 同步CK日期
   * 迁移对应关系: Go语言QueryItemSnapshotMappings返回的syncCkDate
   */
  private String syncCkDate;
}
