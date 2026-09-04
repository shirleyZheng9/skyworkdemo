package com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.mysql.impl;

import com.iwhalecloud.bote.entity.loop.evaluation.ExptTurnResultFilterKeyMappingEntity;
import com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.mysql.IExptTurnResultFilterKeyMappingDAO;
import com.iwhalecloud.bote.mapper.loop.evaluation.ExptTurnResultFilterKeyMappingMapper;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 实验轮次结果过滤键映射DAO实现类
 * 迁移对应关系: Go语言backend/modules/evaluation/infra/repo/experiment/mysql/expt_turn_result_filter_key_mapping.go
 *
 * @author Generated
 * @since 2025-01-27
 */
@Repository
@RequiredArgsConstructor
public class ExptTurnResultFilterKeyMappingDAOImpl implements IExptTurnResultFilterKeyMappingDAO {
  private final ExptTurnResultFilterKeyMappingMapper exptTurnResultFilterKeyMappingMapper;

  /**
   * 根据实验ID获取过滤键映射
   * 迁移对应关系: Go语言ExptTurnResultFilterKeyMappingDAOImpl.GetByExptID
   */
  @Override
  public List<ExptTurnResultFilterKeyMappingEntity> getByExptId(Long spaceId, Long exptId) {
    try {
      return exptTurnResultFilterKeyMappingMapper.getByExptId(spaceId, exptId);
    }
    catch (Exception e) {
      throw new BssException("根据实验ID获取过滤键映射失败: " + e.getMessage(), e);
    }
  }

  /**
   * 插入过滤键映射
   * 迁移对应关系: Go语言ExptTurnResultFilterKeyMappingDAOImpl.Insert
   * 使用先查询再决定插入或更新的方式，兼容所有数据库
   */
  @Override
  public void insert(List<ExptTurnResultFilterKeyMappingEntity> exptTurnResultFilterKeyMappings) {
    try {
      if (exptTurnResultFilterKeyMappings == null || exptTurnResultFilterKeyMappings.isEmpty()) {
        return;
      }

      List<ExptTurnResultFilterKeyMappingEntity> toInsert = new java.util.ArrayList<>();
      List<ExptTurnResultFilterKeyMappingEntity> toUpdate = new java.util.ArrayList<>();

      for (ExptTurnResultFilterKeyMappingEntity mapping : exptTurnResultFilterKeyMappings) {
        boolean exists = exptTurnResultFilterKeyMappingMapper.existsById(
          mapping.getId(), mapping.getSpaceId(), mapping.getExptId());
        if (exists) {
          toUpdate.add(mapping);
        } else {
          toInsert.add(mapping);
        }
      }

      if (!toInsert.isEmpty()) {
        exptTurnResultFilterKeyMappingMapper.insert(toInsert);
      }
      if (!toUpdate.isEmpty()) {
        for (ExptTurnResultFilterKeyMappingEntity mapping : toUpdate) {
          exptTurnResultFilterKeyMappingMapper.updateOne(mapping);
        }
      }
    }
    catch (Exception e) {
      throw new BssException("插入过滤键映射失败: " + e.getMessage(), e);
    }
  }
}
