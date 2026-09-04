package com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.ck.impl;

import com.iwhalecloud.bote.entity.loop.evaluation.ExptTurnResultFilterEntity;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.dto.ExptTurnResultFilterQueryCond;
import com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.ck.IExptTurnResultFilterDAO;
import com.iwhalecloud.bote.mapper.loop.evaluation.ExptTurnResultFilterMapper;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 实验轮次结果过滤器DAO实现类
 * 迁移对应关系: Go语言backend/modules/evaluation/infra/repo/experiment/ck/expt_turn_result_filter.go
 *
 * @author Generated
 * @since 2025-01-27
 */
@Repository
@RequiredArgsConstructor
public class ExptTurnResultFilterDAOImpl implements IExptTurnResultFilterDAO {
  private final ExptTurnResultFilterMapper exptTurnResultFilterMapper;

  /**
   * 保存过滤器数据
   * 迁移对应关系: Go语言exptTurnResultFilterDAOImpl.Save
   */
  @Override
  public void save(List<ExptTurnResultFilterEntity> filter) {
    try {
      if (filter == null || filter.isEmpty()) {
        return;
      }
      exptTurnResultFilterMapper.save(filter);
    }
    catch (Exception e) {
      throw new BssException("保存过滤器数据失败: " + e.getMessage(), e);
    }
  }

  /**
   * 查询项目ID状态
   * 迁移对应关系: Go语言exptTurnResultFilterDAOImpl.QueryItemIDStates
   */
  @Override
  public Map<String, Integer> queryItemIdStates(ExptTurnResultFilterQueryCond cond) {
    try {
      List<Map<String, Object>> results = exptTurnResultFilterMapper.queryItemIdStates(cond);
      return results.stream()
        .filter(result -> result.get("item_id") != null && result.get("status") != null)
        .collect(Collectors.toMap(
          result -> result.get("item_id").toString(),
          result -> {
            Object status = result.get("status");
            if (status instanceof Integer) {
              return (Integer) status;
            }
            else if (status instanceof Long) {
              return ((Long) status).intValue();
            }
            else if (status instanceof Number) {
              return ((Number) status).intValue();
            }
            return 0;
          }
        ));
    }
    catch (Exception e) {
      throw new BssException("查询项目ID状态失败: " + e.getMessage(), e);
    }
  }

  /**
   * 根据实验ID和项目ID获取数据
   * 迁移对应关系: Go语言exptTurnResultFilterDAOImpl.GetByExptIDItemIDs
   */
  @Override
  public List<ExptTurnResultFilterEntity> getByExptIdItemIds(String spaceId, String exptId, String createdDate, List<String> itemIds) {
    try {
      return exptTurnResultFilterMapper.getByExptIdItemIds(spaceId, exptId, createdDate, itemIds);
    }
    catch (Exception e) {
      throw new BssException("根据实验ID和项目ID获取数据失败: " + e.getMessage(), e);
    }
  }
}
