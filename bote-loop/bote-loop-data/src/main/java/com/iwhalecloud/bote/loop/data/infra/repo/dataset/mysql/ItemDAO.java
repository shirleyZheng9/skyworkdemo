package com.iwhalecloud.bote.loop.data.infra.repo.dataset.mysql;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.entity.loop.data.dataset.DatasetItemEntity;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.ItemIdentity;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.dto.ListItemsParams;
import java.util.List;

/**
 * 数据集项目DAO接口
 * 迁移对应关系: Go语言IItemDAO
 * - 功能: 提供数据集项目数据访问接口
 * - 方法定义: 各种数据集项目数据操作方法
 * <p>
 * Java实现说明:
 * - 对应Go的IItemDAO接口
 * - 使用Java接口定义，包含数据集项目数据访问方法
 * - 提供数据集项目数据CRUD操作
 * <p>
 * 技术栈迁移:
 * - Go接口 -> Java接口
 * - Go上下文 -> Java上下文
 * - Go错误处理 -> Java异常处理
 */
public interface ItemDAO {

  /**
   * 统计项目数量
   * 迁移对应关系: Go语言IItemDAO.CountItems
   * - 功能: 统计项目数量
   * - 参数: params - 查询参数
   * - 返回: 项目数量
   * - 用途: 统计项目数量
   */
  Long countItems(ListItemsParams params);

  /**
   * 批量创建项目
   * 迁移对应关系: Go语言IItemDAO.MCreateItems
   * - 功能: 批量创建项目
   * - 参数: items - 项目列表
   * - 返回: 新写入的项目数量（不包含update on conflict部分）
   * - 用途: 批量创建项目
   */
  Long mCreateItems(List<DatasetItemEntity> items);

  /**
   * 列表查询项目
   * 迁移对应关系: Go语言IItemDAO.ListItems
   * - 功能: 列表查询项目
   * - 参数: params - 查询参数
   * - 返回: 项目列表和分页结果
   * - 用途: 分页查询项目
   */
  PageInfo<DatasetItemEntity> listItems(ListItemsParams params);

  /**
   * 更新项目
   * 迁移对应关系: Go语言IItemDAO.UpdateItem
   * - 功能: 更新项目
   * - 参数: item - 项目对象
   * - 用途: 更新项目
   */
  void updateItem(DatasetItemEntity item);

  /**
   * 删除项目
   * 迁移对应关系: Go语言IItemDAO.DeleteItems
   * - 功能: 删除项目
   * - 参数: spaceID - 空间ID, ids - 项目ID列表
   * - 用途: 删除项目
   */
  void deleteItems(Long spaceID, List<Long> ids);

  /**
   * 归档项目
   * 迁移对应关系: Go语言IItemDAO.ArchiveItems
   * - 功能: 归档项目
   * - 参数: spaceID - 空间ID, delVN - 删除版本号, ids - 项目ID列表
   * - 用途: 归档项目
   */
  void archiveItems(Long spaceID, Long delVN, List<Long> ids);

  /**
   * 清空数据集
   * 迁移对应关系: Go语言IItemDAO.ClearDataset
   * - 功能: 清空数据集所有项目
   * - 参数: spaceID - 空间ID, datasetID - 数据集ID, delVN - 删除版本号
   * - 返回: 项目身份列表
   * - 用途: 清空数据集
   */
  List<ItemIdentity> clearDataset(Long spaceID, Long datasetID, Long delVN);
}
