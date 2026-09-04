package com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.mysql.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.entity.loop.evaluation.ExperimentEntity;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptListFilter;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.OrderBy;
import com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.mysql.IExptDAO;
import com.iwhalecloud.bote.mapper.loop.evaluation.ExptMapper;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Repository;

/**
 * 实验DAO实现类
 * 迁移对应关系: Go语言backend/modules/evaluation/infra/repo/experiment/mysql/expt.go
 *
 * @author Generated
 * @since 2025-01-27
 */
@Repository
@RequiredArgsConstructor
public class ExptDAOImpl implements IExptDAO {
  private final ExptMapper exptMapper;

  /**
   * 创建实验
   * 迁移对应关系: Go语言exptDAOImpl.Create
   */
  @Override
  public void create(ExperimentEntity expt) {
    try {
      exptMapper.create(expt);
    }
    catch (Exception e) {
      throw new BssException("创建实验失败: " + e.getMessage(), e);
    }
  }

  /**
   * 更新实验
   * 迁移对应关系: Go语言exptDAOImpl.Update
   */
  @Override
  public void update(ExperimentEntity expt) {
    try {
      exptMapper.update(expt);
    }
    catch (Exception e) {
      throw new BssException("更新实验失败: " + e.getMessage(), e);
    }
  }

  /**
   * 删除实验
   * 迁移对应关系: Go语言exptDAOImpl.Delete
   */
  @Override
  public void delete(Long id) {
    try {
      exptMapper.delete(id);
    }
    catch (Exception e) {
      throw new BssException("删除实验失败: " + e.getMessage(), e);
    }
  }

  /**
   * 批量删除实验
   * 迁移对应关系: Go语言exptDAOImpl.MDelete
   */
  @Override
  public void mDelete(List<Long> ids) {
    try {
      exptMapper.mDelete(ids);
    }
    catch (Exception e) {
      throw new BssException("批量删除实验失败: " + e.getMessage(), e);
    }
  }

  /**
   * 分页查询实验列表
   * 迁移对应关系: Go语言exptDAOImpl.List
   */
  @Override
  public PageInfo<ExperimentEntity> list(Integer pageNumber, Integer size, ExptListFilter filter, List<OrderBy> orders, Long spaceId, Long catalogItemId) {
    RowBounds rowBounds = RowBounds.DEFAULT;
    if (pageNumber != null && pageNumber > 0 && size != null && size > 0) {
      int offset = (pageNumber - 1) * size;
      rowBounds = new RowBounds(offset, size);
    }
    //noinspection resource
    Page<ExperimentEntity> pageResult = exptMapper.list(filter, orders, spaceId, catalogItemId, rowBounds); //NOPMD - suppressed CloseResource - 不需要关闭
    return pageResult.toPageInfo();
  }

  /**
   * 根据名称获取实验
   * 迁移对应关系: Go语言exptDAOImpl.GetByName
   */
  @Override
  public ExperimentEntity getByName(String name, Long spaceId) {
    try {
      return exptMapper.getByName(name, spaceId);
    }
    catch (Exception e) {
      throw new BssException("根据名称获取实验失败: " + e.getMessage(), e);
    }
  }

  /**
   * 根据ID获取实验
   * 迁移对应关系: Go语言exptDAOImpl.GetByID
   */
  @Override
  public ExperimentEntity getById(Long id) {
    try {
      return exptMapper.getById(id);
    }
    catch (Exception e) {
      throw new BssException("根据ID获取实验失败: " + e.getMessage(), e);
    }
  }

  /**
   * 批量根据ID获取实验
   * 迁移对应关系: Go语言exptDAOImpl.MGetByID
   */
  @Override
  public List<ExperimentEntity> mGetById(List<Long> ids) {
    if (ids == null || ids.isEmpty()) {
      return new ArrayList<>();
    }
    try {
      return exptMapper.mGetById(ids);
    }
    catch (Exception e) {
      throw new BssException("批量根据ID获取实验失败: " + e.getMessage(), e);
    }
  }
}
