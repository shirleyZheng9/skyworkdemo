package com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.entity.loop.evaluation.ExperimentEntity;
import com.iwhalecloud.bote.entity.loop.evaluation.ExptEvaluatorRefEntity;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Experiment;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptEvaluatorRef;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptListFilter;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.OrderBy;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.IExperimentRepo;
import com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.mysql.IExptDAO;
import com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.mysql.IExptEvaluatorRefDAO;
import com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.mysql.convertor.ExptConvertor;
import com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.mysql.convertor.ExptEvaluatorRefConvertor;
import com.iwhalecloud.bote.loop.infra.idgen.IIDGenerator;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 实验REPO实现类
 * - 功能: 实验数据访问实现
 * - 主要方法:
 * * create - 创建实验
 * * update - 更新实验
 * * delete - 删除实验
 * * mDelete - 批量删除实验
 * * list - 分页查询实验列表
 * * getById - 根据ID获取实验
 * * mGetById - 批量根据ID获取实验
 * * mGetBasicById - 批量获取实验基本信息
 * * getByName - 根据名称获取实验
 * * getEvaluatorRefByExptIds - 根据实验ID获取评估器引用
 */
@Component
@RequiredArgsConstructor
public class ExptRepoImpl implements IExperimentRepo {
  private static final Logger logger = LoggerFactory.getLogger(ExptRepoImpl.class);

  private final IIDGenerator iidGenerator;
  private final IExptDAO exptDAO;
  private final IExptEvaluatorRefDAO exptEvaluatorRefDAO;

  @Override
  public void create(Experiment expt, List<ExptEvaluatorRef> exptEvaluatorRefs) {
    // 转换实验DO为PO
    ExperimentEntity exptPO = ExptConvertor.convertToPO(expt);
    // 创建实验
    exptDAO.create(exptPO);
    // 生成评估器引用ID
    List<Long> ids = iidGenerator.genMultiIds(exptEvaluatorRefs.size());
    for (int i = 0; i < exptEvaluatorRefs.size(); i++) {
      exptEvaluatorRefs.get(i).setId(ids.get(i));
    }
    // 转换评估器引用DO为PO
    List<ExptEvaluatorRefEntity> exptEvaluatorRefPOs = ExptEvaluatorRefConvertor.convertToPO(exptEvaluatorRefs);
    // 创建评估器引用
    exptEvaluatorRefDAO.create(exptEvaluatorRefPOs);
  }

  @Override
  public void update(Experiment expt) {
    ExperimentEntity exptPO = ExptConvertor.convertToPO(expt);
    exptDAO.update(exptPO);
  }

  @Override
  public void delete(Long id, Long spaceId) {
    exptDAO.delete(id);
  }

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public void mDelete(List<Long> ids, Long spaceId) {
    // 记录批量删除日志
    logger.info("批量删除实验, id: " + ids);
    exptDAO.mDelete(ids);
  }

  @Override
  public PageInfo<Experiment> list(Integer pageNumber, Integer size, ExptListFilter filter, List<OrderBy> orders, Long spaceId) {
    return list(pageNumber, size, filter, orders, spaceId, null);
  }

  @Override
  public PageInfo<Experiment> list(Integer pageNumber, Integer size, ExptListFilter filter, List<OrderBy> orders, Long spaceId, Long catalogItemId) {
    // 查询实验PO列表
    PageInfo<ExperimentEntity> exptPOs = exptDAO.list(pageNumber, size, filter, orders, spaceId, catalogItemId);
    // 提取实验ID列表
    List<Long> exptIds = exptPOs.getList().stream()
      .map(ExperimentEntity::getId)
      .collect(Collectors.toList());
    // 查询评估器引用
    List<ExptEvaluatorRefEntity> refs = exptEvaluatorRefDAO.mGetByExptId(exptIds, spaceId);
    // 按实验ID分组评估器引用
    Map<Long, List<ExptEvaluatorRefEntity>> refsByExptId = refs.stream()
      .collect(Collectors.groupingBy(ExptEvaluatorRefEntity::getExptId));
    return exptPOs.convert(exptPO -> {
      List<ExptEvaluatorRefEntity> exptRefs = refsByExptId.getOrDefault(exptPO.getId(), new ArrayList<>());
      return ExptConvertor.convertToDO(exptPO, exptRefs);
    });
  }

  @Override
  public Experiment getById(Long id, Long spaceId) {
    List<Experiment> expts = mGetById(List.of(id), spaceId);
    if (expts.isEmpty()) {
      throw new BssException("实验不存在");
    }
    return expts.get(0);
  }

  @Override
  public List<Experiment> mGetById(List<Long> ids, Long spaceId) {
    // 查询实验PO列表
    List<ExperimentEntity> exptPOs = exptDAO.mGetById(ids);
    // 提取实验ID列表
    List<Long> exptIds = exptPOs.stream()
      .map(ExperimentEntity::getId)
      .collect(Collectors.toList());
    // 查询评估器引用
    List<ExptEvaluatorRefEntity> refs = exptEvaluatorRefDAO.mGetByExptId(exptIds, spaceId);
    // 按实验ID分组评估器引用
    Map<Long, List<ExptEvaluatorRefEntity>> refsByExptId = refs.stream()
      .collect(Collectors.groupingBy(ExptEvaluatorRefEntity::getExptId));
    // 转换PO为DO
    List<Experiment> expts = new ArrayList<>();
    for (ExperimentEntity exptPO : exptPOs) {
      List<ExptEvaluatorRefEntity> exptRefs = refsByExptId.getOrDefault(exptPO.getId(), new ArrayList<>());
      Experiment expt = ExptConvertor.convertToDO(exptPO, exptRefs);
      expts.add(expt);
    }
    return expts;
  }

  @Override
  public List<Experiment> mGetBasicById(List<Long> ids) {
    // 查询实验PO列表
    List<ExperimentEntity> exptPOs = exptDAO.mGetById(ids);
    // 转换PO为DO（不包含评估器引用）
    List<Experiment> expts = new ArrayList<>();
    for (ExperimentEntity exptPO : exptPOs) {
      Experiment expt = ExptConvertor.convertToDO(exptPO, null);
      expts.add(expt);
    }
    return expts;
  }

  @Override
  public Experiment getByName(String name, Long spaceId) {
    ExperimentEntity exptPO = exptDAO.getByName(name, spaceId);
    if (exptPO == null) {
      return null;
    }
    return ExptConvertor.convertToDO(exptPO, null);
  }

  @Override
  public List<ExptEvaluatorRef> getEvaluatorRefByExptIds(List<Long> exptIds, Long spaceId) {
    List<ExptEvaluatorRefEntity> refPOs = exptEvaluatorRefDAO.mGetByExptId(exptIds, spaceId);
    return ExptEvaluatorRefConvertor.convertToDO(refPOs);
  }
}
