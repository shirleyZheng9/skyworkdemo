package com.iwhalecloud.bote.mapper.loop.evaluation;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.entity.loop.evaluation.ExperimentEntity;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptListFilter;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.OrderBy;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

import java.util.List;

/**
 * 实验Mapper接口
 * 迁移对应关系: Go语言backend/modules/evaluation/infra/repo/experiment/mysql/expt.go
 *
 * @author Generated
 * @since 2025-01-27
 */
public interface ExptMapper {

  /**
   * 创建实验
   * 迁移对应关系: Go语言IExptDAO.Create
   */
  int create(ExperimentEntity expt);

  /**
   * 更新实验
   * 迁移对应关系: Go语言IExptDAO.Update
   */
  int update(ExperimentEntity expt);

  /**
   * 删除实验
   * 迁移对应关系: Go语言IExptDAO.Delete
   */
  int delete(@Param("id") Long id);

  /**
   * 批量删除实验
   * 迁移对应关系: Go语言IExptDAO.MDelete
   */
  int mDelete(@Param("ids") List<Long> ids);

  /**
   * 分页查询实验列表
   * 迁移对应关系: Go语言IExptDAO.List
   *
   * @param filter 过滤条件
   * @param orders 排序条件
   * @param spaceId 空间ID
   * @param rowBounds 分页参数
   * @return 实验分页列表
   */
  Page<ExperimentEntity> list(@Param("filter") ExptListFilter filter,
                              @Param("orders") List<OrderBy> orders,
                              @Param("spaceId") Long spaceId,
                              @Param("catalogItemId") Long catalogItemId,
                              RowBounds rowBounds);

  /**
   * 根据名称获取实验
   * 迁移对应关系: Go语言IExptDAO.GetByName
   */
  ExperimentEntity getByName(@Param("name") String name, @Param("spaceId") Long spaceId);

  /**
   * 根据ID获取实验
   * 迁移对应关系: Go语言IExptDAO.GetByID
   */
  ExperimentEntity getById(@Param("id") Long id);

  /**
   * 批量根据ID获取实验
   * 迁移对应关系: Go语言IExptDAO.MGetByID
   */
  List<ExperimentEntity> mGetById(@Param("ids") List<Long> ids);
}
