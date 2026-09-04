package com.iwhalecloud.bote.loop.prompt.domain.repo.dto;

import com.iwhalecloud.bote.entity.loop.prompt.PromptBasicEntity;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分页查询Prompt基础信息结果
 * 迁移对应关系: Go语言List方法返回值
 * - 功能: 分页查询Prompt基础信息的结果类
 * - 字段定义:
 * * basicPOs: List<PromptBasicPO> - Prompt基础信息列表
 * * total: Long - 总记录数
 * <p>
 * Java实现说明:
 * - 对应Go的List方法返回值
 * - 使用Lombok注解简化代码
 * - 用于分页查询结果的封装
 * <p>
 * 技术栈迁移:
 * - Go []*model.PromptBasic -> Java List<PromptBasicPO>
 * - Go int64 -> Java Long
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListPromptBasicResult {

  /**
   * Prompt基础信息列表
   * 迁移对应关系: Go语言List方法返回的basicPOs
   * - 功能: 查询结果列表
   * - 类型: Go的[]*model.PromptBasic对应Java的List<PromptBasicPO>
   */
  private List<PromptBasicEntity> basicPOs;

  /**
   * 总记录数
   * 迁移对应关系: Go语言List方法返回的total
   * - 功能: 总记录数
   * - 类型: Go的int64对应Java的Long
   */
  private Long total;
}
