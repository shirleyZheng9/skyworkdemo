package com.iwhalecloud.bote.loop.evaluation.domain.repo;

import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptTurnResultFilterAccelerator;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptTurnResultFilterEntityDO;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptTurnResultFilterKeyMapping;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ItemRunState;
import java.util.List;
import java.util.Map;

/**
 * 实验轮次结果过滤器REPO接口
 * 迁移对应关系: Go语言IExptTurnResultFilterRepo接口
 * - 功能: 实验轮次结果过滤器数据访问接口
 * - 主要方法:
 * * save - 保存过滤器数据
 * * queryItemIdStates - 查询项目ID状态
 * * getExptTurnResultFilterKeyMappings - 获取过滤器键映射
 * * insertExptTurnResultFilterKeyMappings - 插入过滤器键映射
 * * getByExptIdItemIds - 根据实验ID和项目ID获取数据
 * <p>
 * Java实现说明:
 * - 对应Go的IExptTurnResultFilterRepo接口
 * - 使用领域层接口定义
 * - 支持复杂过滤条件查询
 * - 支持批量操作
 * <p>
 * 技术栈迁移:
 * - Go context.Context -> Java方法参数
 * - Go error返回 -> Java异常处理
 * - Go []*entity.ExptTurnResultFilterEntity -> Java List<ExptTurnResultFilter>
 * - Go map[int64]entity.ItemRunState -> Java Map<Long, ItemRunState>
 * - Go []*entity.ExptTurnResultFilterKeyMapping -> Java List<ExptTurnResultFilterKeyMapping>
 */
public interface IExptTurnResultFilterRepo {

  /**
   * 保存过滤器数据
   * 迁移对应关系: Go语言Save方法
   * - 功能: 批量保存实验轮次结果过滤器数据
   * - 参数: filters - 过滤器数据列表
   * - 异常: 保存失败时抛出BssException
   */
  void save(List<ExptTurnResultFilterEntityDO> filters);

  /**
   * 查询项目ID状态
   * 迁移对应关系: Go语言QueryItemIDStates方法
   * - 功能: 根据过滤条件查询项目ID对应的状态
   * - 参数: filter - 过滤加速器条件
   * - 返回: 项目ID到状态的映射
   * - 异常: 查询失败时抛出BssException
   */
  Map<Long, ItemRunState> queryItemIdStates(ExptTurnResultFilterAccelerator filter);

  /**
   * 获取实验轮次结果过滤器键映射
   * 迁移对应关系: Go语言GetExptTurnResultFilterKeyMappings方法
   * - 功能: 根据空间ID和实验ID获取过滤器键映射
   * - 参数: spaceId - 空间ID
   * - 参数: exptId - 实验ID
   * - 返回: 过滤器键映射列表
   * - 异常: 查询失败时抛出BssException
   */
  List<ExptTurnResultFilterKeyMapping> getExptTurnResultFilterKeyMappings(Long spaceId, Long exptId);

  /**
   * 插入实验轮次结果过滤器键映射
   * 迁移对应关系: Go语言InsertExptTurnResultFilterKeyMappings方法
   * - 功能: 批量插入过滤器键映射
   * - 参数: mappings - 过滤器键映射列表
   * - 异常: 插入失败时抛出BssException
   */
  void insertExptTurnResultFilterKeyMappings(List<ExptTurnResultFilterKeyMapping> mappings);

  /**
   * 根据实验ID和项目ID获取数据
   * 迁移对应关系: Go语言GetByExptIDItemIDs方法
   * - 功能: 根据实验ID和项目ID列表获取过滤器数据
   * - 参数: spaceId - 空间ID
   * - 参数: exptId - 实验ID
   * - 参数: createdDate - 创建日期
   * - 参数: itemIds - 项目ID列表
   * - 返回: 过滤器数据列表
   * - 异常: 查询失败时抛出BssException
   */
  List<ExptTurnResultFilterEntityDO> getByExptIdItemIds(String spaceId, String exptId, String createdDate, List<String> itemIds);
}
