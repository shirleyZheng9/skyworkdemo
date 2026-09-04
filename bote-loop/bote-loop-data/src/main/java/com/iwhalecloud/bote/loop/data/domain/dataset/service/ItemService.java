package com.iwhalecloud.bote.loop.data.domain.dataset.service;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.Item;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.dto.DatasetWithSchema;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.dto.IndexedItem;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.dto.MAddItemOpt;
import java.util.List;

/**
 * 项目服务接口
 * 迁移对应关系: Go语言service.IItemService
 * - 功能: 提供项目相关的业务逻辑服务
 * - 方法定义: 各种项目操作方法
 * <p>
 * Java实现说明:
 * - 对应Go的service.IItemService接口
 * - 使用Java接口定义，包含项目服务方法
 * - 提供项目CRUD和业务逻辑操作
 * <p>
 * 技术栈迁移:
 * - Go接口 -> Java接口
 * - Go上下文 -> Java上下文
 * - Go错误处理 -> Java异常处理
 */
public interface ItemService {

  /**
   * 批量创建项目
   * 迁移对应关系: Go语言service.IItemService.BatchCreateItems
   * - 功能: 创建新 items，支持 itemKey 幂等锁
   * - 参数: dataset - 数据集和模式, items - 索引项目列表, opt - 添加选项
   * - 返回: 添加的项目列表
   * - 用途: 批量创建项目
   */
  List<IndexedItem> batchCreateItems(DatasetWithSchema dataset, List<IndexedItem> items, MAddItemOpt opt);

  /**
   * 批量获取项目
   * 迁移对应关系: Go语言service.IItemService.BatchGetItems
   * - 功能: 获取 items
   * - 参数: spaceId - 空间ID, datasetId - 数据集ID, itemIds - 项目ID列表
   * - 返回: 项目列表
   * - 用途: 批量获取项目
   */
  PageInfo<Item> batchGetItems(Long spaceId, Long datasetId, List<Long> itemIds);

  /**
   * 获取项目
   * 迁移对应关系: Go语言service.IItemService.GetItem
   * - 功能: 获取 item
   * - 参数: spaceId - 空间ID, datasetId - 数据集ID, itemId - 项目ID
   * - 返回: 项目
   * - 用途: 获取单个项目
   */
  Item getItem(Long spaceId, Long datasetId, Long itemId);

  /**
   * 加载项目数据
   * 迁移对应关系: Go语言service.IItemService.LoadItemData
   * - 功能: 填充 item 的数据内容，为多模态文件签发 URL
   * - 参数: items - 项目列表
   * - 用途: 加载项目数据
   */
  void loadItemData(Item... items);

  /**
   * 归档并创建新项目
   * 迁移对应关系: Go语言service.IItemService.ArchiveAndCreateItem
   * - 功能: 归档并创建新的 item，用于更新有版本引用的 item
   * - 参数: dataset - 数据集和模式, oldId - 旧ID, item - 项目
   * - 用途: 归档并创建新项目
   */
  void archiveAndCreateItem(DatasetWithSchema dataset, Long oldId, Item item);

  /**
   * 更新项目
   * 迁移对应关系: Go语言service.IItemService.UpdateItem
   * - 功能: 更新 item，用于无版本引用的 item
   * - 参数: dataset - 数据集和模式, item - 项目
   * - 用途: 更新项目
   */
  void updateItem(DatasetWithSchema dataset, Item item);

  /**
   * 批量删除项目
   * 迁移对应关系: Go语言service.IItemService.BatchDeleteItems
   * - 功能: 删除 items
   * - 参数: dataset - 数据集和模式, items - 项目列表
   * - 用途: 批量删除项目
   */
  void batchDeleteItems(DatasetWithSchema dataset, Item... items);

  /**
   * 清空数据集
   * 迁移对应关系: Go语言service.IItemService.ClearDataset
   * - 功能: 清空 dataset 所有 items
   * - 参数: dataset - 数据集和模式
   * - 用途: 清空数据集所有项目
   */
  void clearDataset(DatasetWithSchema dataset);
}
