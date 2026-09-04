package com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.mysql;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.entity.loop.evaluation.ExperimentEntity;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptListFilter;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.OrderBy;
import java.util.List;

/**
 * 实验DAO接口
 * 迁移对应关系: Go语言backend/modules/evaluation/infra/repo/experiment/mysql/expt.go
 *
 * @author Generated
 * @since 2025-01-27
 */
public interface IExptDAO {

  /**
   * 创建实验
   * 迁移对应关系: Go语言IExptDAO.Create
   *
   * @param expt 实验实体
   */
  void create(ExperimentEntity expt);

  /**
   * 更新实验
   * 迁移对应关系: Go语言IExptDAO.Update
   *
   * @param expt 实验实体
   */
  void update(ExperimentEntity expt);

  /**
   * 删除实验
   * 迁移对应关系: Go语言IExptDAO.Delete
   *
   * @param id 实验ID
   */
  void delete(Long id);

  /**
   * 批量删除实验
   * 迁移对应关系: Go语言IExptDAO.MDelete
   *
   * @param ids 实验ID列表
   */
  void mDelete(List<Long> ids);

  /**
   * 分页查询实验列表
   * 迁移对应关系: Go语言IExptDAO.List
   *
   * @param pageNumber 页码
   * @param size 页大小
   * @param filter 过滤条件
   * @param orders 排序条件
   * @param spaceId 空间ID
   * @return 实验列表和总数
   */
  PageInfo<ExperimentEntity> list(Integer pageNumber, Integer size, ExptListFilter filter, List<OrderBy> orders, Long spaceId, Long catalogItemId);

  /**
   * 根据名称获取实验
   * 迁移对应关系: Go语言IExptDAO.GetByName
   *
   * @param name 实验名称
   * @param spaceId 空间ID
   * @return 实验实体
   */
  ExperimentEntity getByName(String name, Long spaceId);

  /**
   * 根据ID获取实验
   * 迁移对应关系: Go语言IExptDAO.GetByID
   *
   * @param id 实验ID
   * @return 实验实体
   */
  ExperimentEntity getById(Long id);

  /**
   * 批量根据ID获取实验
   * 迁移对应关系: Go语言IExptDAO.MGetByID
   *
   * @param ids 实验ID列表
   * @return 实验列表
   */
  List<ExperimentEntity> mGetById(List<Long> ids);
}
