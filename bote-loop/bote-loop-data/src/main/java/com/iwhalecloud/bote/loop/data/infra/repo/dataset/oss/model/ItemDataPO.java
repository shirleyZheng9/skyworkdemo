package com.iwhalecloud.bote.loop.data.infra.repo.dataset.oss.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.FieldData;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.ItemData;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 项目数据持久化对象
 * 迁移对应关系: Go语言model.ItemDataPO
 * - 功能: 项目数据持久化对象
 * - 字段定义: 各种项目数据字段
 * <p>
 * Java实现说明:
 * - 对应Go的model.ItemDataPO结构体
 * - 使用Lombok注解简化代码
 * - 提供项目数据序列化支持
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go JSON标签 -> Java Jackson注解
 * - Go指针 -> Java对象引用
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemDataPO {

  /**
   * 数据内容
   * 迁移对应关系: Go语言ItemDataPO.Data
   * - 功能: 数据内容
   * - 类型: 字段数据列表
   * - 用途: 存储项目数据内容
   */
  @JsonProperty("data")
  private List<FieldData> data;

  /**
   * 多轮数据内容
   * 迁移对应关系: Go语言ItemDataPO.RepeatedData
   * - 功能: 多轮数据内容，与Data互斥
   * - 类型: 项目数据列表
   * - 用途: 存储多轮数据内容
   */
  @JsonProperty("repeated_data")
  private List<ItemData> repeatedData;
}
