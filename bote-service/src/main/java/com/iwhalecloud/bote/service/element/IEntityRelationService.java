package com.iwhalecloud.bote.service.element;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.base.EntityRelationResultDTO;
import com.iwhalecloud.bote.dto.base.EntityInfoDTO;
import com.iwhalecloud.bote.dto.base.query.EntityPageQueryParams;
import com.iwhalecloud.bote.dto.base.query.EntityRelationQueryParams;
import java.util.List;

/**
 * 实体关系查询服务
 *
 * @author qian.sisheng
 * @since 2025-12-03
 */
public interface IEntityRelationService {

  /**
   * 获取血缘关系树预览(只统计数量)
   *
   * @param queryParam 分组详情查询参数
   * @return 血缘关系树预览
   */
  EntityRelationResultDTO queryEntityRelationTree(EntityRelationQueryParams queryParam);

  /**
   * 查询分组详情(点击节点时调用)
   *
   * @param queryParam 分组详情查询参数
   * @return 关联元素详情列表
   */
  List<EntityInfoDTO> queryEntityDetail(EntityRelationQueryParams queryParam);

  /**
   * 分页查询实体列表(分页)
   *
   * @param queryParam 分组详情查询参数
   * @return 实体列表
   */
  PageInfo<?> queryEntityPage(EntityPageQueryParams queryParam);
}
