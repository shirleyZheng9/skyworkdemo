package com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.ck;

import com.iwhalecloud.bote.entity.loop.evaluation.ExptTurnResultFilterEntity;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.dto.ExptTurnResultFilterQueryCond;
import java.util.List;
import java.util.Map;

/**
 * 实验轮次结果过滤器DAO接口
 * 迁移对应关系: Go语言IExptTurnResultFilterDAO
 * - 功能: 实验轮次结果过滤器数据访问接口
 * - 主要方法:
 * * save - 保存过滤器数据
 * * queryItemIdStates - 查询项目ID状态
 * * getByExptIdItemIds - 根据实验ID和项目ID获取数据
 * <p>
 * Java实现说明:
 * - 对应Go的IExptTurnResultFilterDAO接口
 * - 使用MySQL代替ClickHouse
 * - 支持复杂查询条件
 * - 支持分页和排序
 * <p>
 * 技术栈迁移:
 * - Go context.Context -> Java方法参数
 * - Go error返回 -> Java异常处理
 * - Go []*model.ExptTurnResultFilter -> Java List<ExptTurnResultFilterPO>
 * - Go map[string]int32 -> Java Map<String, Integer>
 * - Go []string -> Java List<String>
 */
public interface IExptTurnResultFilterDAO {

  /**
   * 保存过滤器数据
   * 迁移对应关系: Go语言Save
   */
  void save(List<ExptTurnResultFilterEntity> filter);

  /**
   * 查询项目ID状态
   * 迁移对应关系: Go语言QueryItemIDStates
   */
  Map<String, Integer> queryItemIdStates(ExptTurnResultFilterQueryCond cond);

  /**
   * 根据实验ID和项目ID获取数据
   * 迁移对应关系: Go语言GetByExptIDItemIDs
   */
  List<ExptTurnResultFilterEntity> getByExptIdItemIds(String spaceId, String exptId, String createdDate, List<String> itemIds);
}
