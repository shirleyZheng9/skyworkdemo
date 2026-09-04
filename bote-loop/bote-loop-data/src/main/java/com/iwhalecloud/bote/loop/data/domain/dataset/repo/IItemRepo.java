package com.iwhalecloud.bote.loop.data.domain.dataset.repo;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.Item;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.ItemIdentity;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.dto.ListItemsParams;
import java.util.List;

/**
 * 项目仓库接口
 * 迁移对应关系: Go语言repo.IItemRepo
 * - 功能: 提供数据集项目数据访问接口
 * - 方法定义: 各种数据集项目数据操作方法
 * <p>
 * Java实现说明:
 * - 对应Go的repo.IItemRepo接口
 * - 使用Java接口定义，包含数据集项目数据访问方法
 * - 提供数据集项目数据CRUD操作
 * <p>
 * 技术栈迁移:
 * - Go接口 -> Java接口
 * - Go上下文 -> Java上下文
 * - Go错误处理 -> Java异常处理
 */
public interface IItemRepo {

  /**
   * 批量设置项目数据
   * 迁移对应关系: Go语言repo.IItemRepo.MSetItemData
   * - 功能: 批量设置项目数据
   * - 参数: items - 项目列表, provider - 提供者
   * - 返回: 设置数量
   * - 用途: 批量设置项目数据
   */
  Integer mSetItemData(List<Item> items);

  /**
   * 批量获取项目数据
   * 迁移对应关系: Go语言repo.IItemRepo.MGetItemData
   * - 功能: 批量获取项目数据
   * - 参数: items - 项目列表, provider - 提供者
   * - 用途: 批量获取项目数据
   */
  void mGetItemData(List<Item> items);

  /**
   * 批量创建项目
   * 迁移对应关系: Go语言repo.IItemRepo.MCreateItems
   * - 功能: 批量创建项目
   * - 参数: items - 项目列表
   * - 返回: 新写入的项目数量
   * - 用途: 批量创建项目
   */
  Long mCreateItems(List<Item> items);

  /**
   * 列表项目
   * 迁移对应关系: Go语言repo.IItemRepo.ListItems
   * - 功能: 列表项目
   * - 参数: params - 列表参数
   * - 返回: 项目列表
   * - 用途: 列表项目
   */
  PageInfo<Item> listItems(ListItemsParams params);

  /**
   * 统计项目数量
   * 迁移对应关系: Go语言repo.IItemRepo.CountItems
   * - 功能: 统计项目数量
   * - 参数: params - 列表参数
   * - 返回: 数量
   * - 用途: 统计项目数量
   */
  Long countItems(ListItemsParams params);

  /**
   * 更新项目
   * 迁移对应关系: Go语言repo.IItemRepo.UpdateItem
   * - 功能: 更新项目
   * - 参数: item - 项目
   * - 用途: 更新项目
   */
  void updateItem(Item item);

  /**
   * 删除项目
   * 迁移对应关系: Go语言repo.IItemRepo.DeleteItems
   * - 功能: 删除项目
   * - 参数: spaceID - 空间ID, ids - 项目ID列表
   * - 用途: 删除项目
   */
  void deleteItems(Long spaceID, List<Long> ids);

  /**
   * 归档项目
   * 迁移对应关系: Go语言repo.IItemRepo.ArchiveItems
   * - 功能: 归档项目
   * - 参数: spaceID - 空间ID, delVN - 删除版本号, ids - 项目ID列表
   * - 用途: 归档项目
   */
  void archiveItems(Long spaceID, Long delVN, List<Long> ids);

  /**
   * 清空数据集
   * 迁移对应关系: Go语言repo.IItemRepo.ClearDataset
   * - 功能: 清空数据集所有项目
   * - 参数: spaceID - 空间ID, datasetID - 数据集ID, delVN - 删除版本号
   * - 返回: 项目身份列表
   * - 用途: 清空数据集所有项目
   */
  List<ItemIdentity> clearDataset(Long spaceID, Long datasetID, Long delVN);
}
