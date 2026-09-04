package com.iwhalecloud.bote.loop.evaluation.domain.service;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.CreateExptParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Experiment;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptListFilter;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.GetExptTupleOption;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ListExptParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Session;

import java.util.List;

/**
 * 实验配置管理接口（负责实验元数据的增删改查）
 * 对应Go: IExptConfigManager
 */
public interface IExptConfigManager {

  /**
   * 检查名称
   * 对应Go: CheckName
   */
  Boolean checkName(String name, Long spaceId, Session session);

  /**
   * 创建实验
   * 对应Go: CreateExpt
   */
  Experiment createExpt(CreateExptParam req, Session session);

  /**
   * 更新实验
   * 对应Go: Update
   */
  void update(Experiment expt, Session session);

  /**
   * 删除实验
   * 对应Go: Delete
   */
  void delete(Long exptId, Long spaceId, Session session);

  /**
   * 批量删除实验
   * 对应Go: MDelete
   */
  void mDelete(List<Long> exptIds, Long spaceId, Session session);

  /**
   * 查询实验列表
   * */
  PageInfo<Experiment> list(ListExptParam param);

  /**
   * 查询实验原始列表
   * 对应Go: ListExptRaw
   */
  PageInfo<Experiment> listExptRaw(Integer pageNumber, Integer pageSize, Long spaceId, ExptListFilter filter);

  /**
   * 获取实验详情
   * 对应Go: GetDetail
   */
  Experiment getDetail(Long exptId, Long spaceId, Session session, GetExptTupleOption... opts);

  /**
   * 批量获取实验详情
   * 对应Go: MGetDetail
   */
  List<Experiment> mGetDetail(List<Long> exptIds, Long spaceId, Session session);

  /**
   * 获取实验
   * 对应Go: Get
   */
  Experiment get(Long exptId, Long spaceId, Session session);

  /**
   * 批量获取实验
   * 对应Go: MGet
   */
  List<Experiment> mGet(List<Long> exptIds, Long spaceId);

  /**
   * 克隆实验
   * 对应Go: Clone
   */
  Experiment clone(Long exptId, Long spaceId);
}
