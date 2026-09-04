package com.iwhalecloud.bote.loop.evaluation.domain.repo;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Experiment;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptEvaluatorRef;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptListFilter;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.OrderBy;
import java.util.List;

/**
 * 实验仓库接口
 * 对应Go: IExperimentRepo
 */
public interface IExperimentRepo {

  /**
   * 创建实验
   * 对应Go: Create(ctx context.Context, expt *entity.Experiment, exptEvaluatorRefs []*entity.ExptEvaluatorRef) error
   */
  void create(Experiment expt, List<ExptEvaluatorRef> exptEvaluatorRefs);

  /**
   * 更新实验
   * 对应Go: Update(ctx context.Context, expt *entity.Experiment) error
   */
  void update(Experiment expt);

  /**
   * 删除实验
   * 对应Go: Delete(ctx context.Context, id, spaceID int64) error
   */
  void delete(Long id, Long spaceId);

  /**
   * 批量删除实验
   * 对应Go: MDelete(ctx context.Context, ids []int64, spaceID int64) error
   */
  void mDelete(List<Long> ids, Long spaceId);

  /**
   * 列出实验
   * 对应Go: List(ctx context.Context, page, size int32, filter *entity.ExptListFilter, orders []*entity.OrderBy, spaceID int64) ([]*entity.Experiment, int64, error)
   */
  PageInfo<Experiment> list(Integer pageNumber, Integer size, ExptListFilter filter, List<OrderBy> orders, Long spaceId);
  PageInfo<Experiment> list(Integer pageNumber, Integer size, ExptListFilter filter, List<OrderBy> orders, Long spaceId, Long catalogItemId);

  /**
   * 根据ID获取实验
   * 对应Go: GetByID(ctx context.Context, id, spaceID int64) (*entity.Experiment, error)
   */
  Experiment getById(Long id, Long spaceId);

  /**
   * 批量根据ID获取实验
   * 对应Go: MGetByID(ctx context.Context, ids []int64, spaceID int64) ([]*entity.Experiment, error)
   */
  List<Experiment> mGetById(List<Long> ids, Long spaceId);

  /**
   * 批量根据ID获取基础信息
   * 对应Go: MGetBasicByID(ctx context.Context, ids []int64) ([]*entity.Experiment, error)
   */
  List<Experiment> mGetBasicById(List<Long> ids);

  /**
   * 根据名称获取实验
   * 对应Go: GetByName(ctx context.Context, name string, spaceID int64) (*entity.Experiment, bool, error)
   */
  Experiment getByName(String name, Long spaceId);

  /**
   * 根据实验ID获取评估器引用
   * 对应Go: GetEvaluatorRefByExptIDs(ctx context.Context, exptID []int64, spaceID int64) ([]*entity.ExptEvaluatorRef, error)
   */
  List<ExptEvaluatorRef> getEvaluatorRefByExptIds(List<Long> exptIds, Long spaceId);
}
