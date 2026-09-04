package com.iwhalecloud.bote.loop.data.domain.dataset.service.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.Dataset;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetOpType;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.Item;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.ItemDataProperties;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.IDatasetAPI;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.dto.ListItemsParams;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.ItemService;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.dto.DatasetWithSchema;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.dto.IndexedItem;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.dto.MAddItemOpt;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.util.ItemUtils;
import com.iwhalecloud.bote.loop.infra.idgen.IIDGenerator;
import com.iwhalecloud.bote.loop.infra.session.SessionContext;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/**
 * 数据集项目服务实现类
 * 迁移对应关系: Go语言DatasetServiceImpl项目相关方法
 * - 功能: 实现数据集项目相关的业务逻辑
 * - 方法实现: 各种数据集项目操作方法实现
 * <p>
 * Java实现说明:
 * - 对应Go的DatasetServiceImpl项目相关方法
 * - 使用Repository层实现数据访问
 * - 提供数据集项目管理功能
 * <p>
 * 技术栈迁移:
 * - Go GORM -> Java MyBatis
 * - Go上下文 -> Java上下文
 * - Go错误处理 -> Java异常处理
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class ItemServiceImpl implements ItemService {
  private static final Logger logger = LoggerFactory.getLogger(ItemServiceImpl.class);

  private final IDatasetAPI repo;
  private final IIDGenerator idGenerator;

  /**
   * 加载项目数据
   * 迁移对应关系: Go语言DatasetServiceImpl.LoadItemData
   * - 功能: 加载项目数据
   * - 参数: items - 项目列表
   * - 用途: 从存储加载项目数据
   */
  public void loadItemData(Item... items) {
    if (items == null || items.length == 0) {
      return;
    }

    // 按存储类型分组
    Map<String, List<Item>> byStorage = Arrays.stream(items)
      .collect(Collectors.groupingBy(item -> {
        ItemDataProperties props = item.getOrBuildProperties();
        return props.getStorage();
      }));

    for (Map.Entry<String, List<Item>> entry : byStorage.entrySet()) {
      String provider = entry.getKey();
      List<Item> itemList = entry.getValue();

      switch (provider) {
        case "S3":
        case "ABASE":
          repo.mGetItemData(itemList);
          break;
        default:
          // 其他存储类型处理
          break;
      }
    }

    // TODO: 签名附件URL
  }

  /**
   * 归档并创建项目
   * 迁移对应关系: Go语言DatasetServiceImpl.ArchiveAndCreateItem
   * - 功能: 归档旧项目并创建新项目
   * - 参数: ds - 数据集和模式, oldId - 旧项目ID, item - 新项目
   * - 用途: 项目更新
   */
  @Transactional
  public void archiveAndCreateItem(DatasetWithSchema ds, Long oldId, Item item) {
    saveItemData(item);

    repo.archiveItems(ds.getDataset().getSpaceId(), ds.getDataset().getNextVersionNum(), List.of(oldId));

    item.setId(0L);
    repo.mCreateItems(List.of(item));
  }

  /**
   * 更新项目
   * 迁移对应关系: Go语言DatasetServiceImpl.UpdateItem
   * - 功能: 更新项目
   * - 参数: ds - 数据集和模式, item - 项目
   * - 用途: 项目更新
   */
  @Transactional
  public void updateItem(DatasetWithSchema ds, Item item) {
    saveItemData(item);
    repo.updateItem(item);
  }

  /**
   * 批量删除项目
   * 迁移对应关系: Go语言DatasetServiceImpl.BatchDeleteItems
   * - 功能: 批量删除项目
   * - 参数: ds - 数据集和模式, items - 项目列表
   * - 用途: 批量删除项目
   */
  @Transactional
  public void batchDeleteItems(DatasetWithSchema ds, Item... items) {
    if (items == null || items.length == 0) {
      return;
    }
    List<Long> idsToArchive = Arrays.stream(items)
      .filter(item -> !item.getAddVN().equals(ds.getDataset().getNextVersionNum()))
      .map(Item::getId)
      .collect(Collectors.toList());

    List<Long> idsToDelete = Arrays.stream(items)
      .filter(item -> item.getAddVN().equals(ds.getDataset().getNextVersionNum()))
      .map(Item::getId)
      .collect(Collectors.toList());

    if (!idsToArchive.isEmpty()) {
//      repo.archiveItems(ds.getDataset().getSpaceId(), ds.getDataset().getNextVersionNum(), idsToArchive);
      repo.deleteItems(ds.getDataset().getSpaceId(), idsToArchive);
    }
    if (!idsToDelete.isEmpty()) {
      repo.deleteItems(ds.getDataset().getSpaceId(), idsToDelete);
    }

    Long n = repo.incrItemCount(ds.getDataset().getId(), -(long) items.length);
    logger.info("delete {} items, archive_ids={}, delete_ids={}, item_count={}",
      items.length, idsToArchive, idsToDelete, n);
  }

  /**
   * 清空数据集
   * 迁移对应关系: Go语言DatasetServiceImpl.ClearDataset
   * - 功能: 清空数据集
   * - 参数: ds - 数据集和模式
   * - 用途: 清空数据集
   */
  @Transactional
  @SuppressWarnings("PMD.GuardLogStatement")
  public void clearDataset(DatasetWithSchema ds) {
    logger.info("dataset {} will be cleared, space_id={}, name={}, vn={}",
      ds.getDataset().getId(), ds.getDataset().getSpaceId(), ds.getDataset().getName(), ds.getDataset().getNextVersionNum());

    repo.patchDataset(
      Dataset.builder()
        .lastOperation(DatasetOpType.CLEAR_DATASET)
        .updatedBy(ds.getDataset().getUpdatedBy())
        .build(),
      Dataset.builder()
        .spaceId(ds.getDataset().getSpaceId())
        .id(ds.getDataset().getId())
        .build()
    );

    repo.clearDataset(ds.getDataset().getSpaceId(), ds.getDataset().getId(), ds.getDataset().getNextVersionNum());
    repo.setItemCount(ds.getDataset().getId(), 0L);
  }

  /**
   * 获取项目
   * 迁移对应关系: Go语言DatasetServiceImpl.GetItem
   * - 功能: 获取项目
   * - 参数: spaceId - 空间ID, datasetId - 数据集ID, itemId - 项目ID
   * - 返回: 项目对象
   * - 用途: 获取单个项目
   */
  public Item getItem(Long spaceId, Long datasetId, Long itemId) {
    ListItemsParams query = ListItemsParams.builder()
      .spaceId(spaceId)
      .datasetId(datasetId)
      .itemIds(List.of(itemId))
      .build();

    PageInfo<Item> items = repo.listItems(query);
    List<Item> list = items.getList();
    if (CollectionUtils.isEmpty(list)) {
      throw new BssException("item " + itemId + " not found");
    }
    return list.getFirst();
  }

  /**
   * 批量获取项目
   * 迁移对应关系: Go语言DatasetServiceImpl.BatchGetItems
   * - 功能: 批量获取项目
   * - 参数: spaceId - 空间ID, datasetId - 数据集ID, itemIds - 项目ID列表
   * - 返回: 项目列表
   * - 用途: 批量获取项目
   */
  public PageInfo<Item> batchGetItems(Long spaceId, Long datasetId, List<Long> itemIds) {
    ListItemsParams query = ListItemsParams.builder()
      .spaceId(spaceId)
      .datasetId(datasetId)
      .itemIds(itemIds)
      .build();

    return repo.listItems(query);
  }

  /**
   * 批量创建项目
   * 迁移对应关系: Go语言DatasetServiceImpl.BatchCreateItems
   * - 功能: 批量创建项目
   * - 参数: ds - 数据集和模式, iitems - 索引项目列表, opt - 添加选项
   * - 返回: 添加的项目列表
   * - 用途: 批量创建项目
   */
  @Transactional
  public List<IndexedItem> batchCreateItems(DatasetWithSchema ds, List<IndexedItem> iitems, MAddItemOpt opt) {
    if (CollectionUtils.isEmpty(iitems)) {
      return List.of();
    }

    List<Item> items = iitems.stream()
      .map(IndexedItem::getItem)
      .collect(Collectors.toList());

    buildNewItems(ds, items);
    Long n = acquireItemCount(ds, (long) items.size(), opt.getPartialAdd());
    if (n <= 0) {
      return List.of();
    }

    List<IndexedItem> added = iitems.subList(0, n.intValue());
    Long count = mCreateItems(items.subList(0, n.intValue()));

    if (n - count > 0) {
      repo.incrItemCount(ds.getDataset().getId(), -(n - count));
    }

    return added;
  }

  /**
   * 构建新项目
   * 迁移对应关系: Go语言DatasetServiceImpl.buildNewItems
   * - 功能: 构建新项目
   * - 参数: ds - 数据集和模式, items - 项目列表
   * - 用途: 设置项目默认值
   */
  private void buildNewItems(DatasetWithSchema ds, List<Item> items) {
    for (Item item : items) {
      item.setId(idGenerator.genId());
      item.setAppId(ds.getDataset().getAppId());
      item.setSpaceId(ds.getDataset().getSpaceId());
      item.setDatasetId(ds.getDataset().getId());
      item.setSchemaId(ds.getSchema().getId());
      item.setItemId(item.getId());
      item.setAddVN(ds.getDataset().getNextVersionNum());
      item.setDelVN(Long.MAX_VALUE);
      item.buildProperties();
      item.setCreatedBy(SessionContext.getCurrentUserId());
      if (item.getItemKey() != null && !item.getItemKey().isEmpty()) {
        item.setItemKey(String.valueOf(item.getItemId()));
      }
    }
  }

  /**
   * 获取项目数量
   * 迁移对应关系: Go语言DatasetServiceImpl.acquireItemCount
   * - 功能: 获取项目数量
   * - 参数: ds - 数据集和模式, want - 期望数量, partial - 是否部分添加
   * - 返回: 实际数量
   * - 用途: 项目数量控制
   */
  private Long acquireItemCount(DatasetWithSchema ds, Long want, boolean partial) {
    Long total = repo.incrItemCount(ds.getDataset().getId(), want);
    Long debt = total - ds.getDataset().getSpec().getMaxItemCount();
    if (debt > 0) {
      if (debt > want) {
        debt = want;
      }
      if (!partial) {
        debt = want;
      }
      logger.info("dataset capacity exceeded, decrease by {}, dataset_id={}", debt, ds.getDataset().getId());
      repo.incrItemCount(ds.getDataset().getId(), -debt);
      return want - debt;
    }
    return want;
  }

  /**
   * 批量创建项目内部实现
   * 迁移对应关系: Go语言DatasetServiceImpl.mCreateItems
   * - 功能: 批量创建项目内部实现
   * - 参数: ds - 数据集和模式, items - 项目列表
   * - 返回: 创建数量
   * - 用途: 项目创建内部逻辑
   */
  private Long mCreateItems(List<Item> items) {
    return saveItems(items);
  }

  /**
   * 保存项目
   * 迁移对应关系: Go语言DatasetServiceImpl.saveItems
   * - 功能: 保存项目
   * - 参数: ds - 数据集和模式, items - 项目列表
   * - 返回: 保存数量
   * - 用途: 项目保存
   */
  private Long saveItems(List<Item> items) {
    saveItemData(items.toArray(new Item[0]));
    return repo.mCreateItems(items);
  }

  /**
   * 保存项目数据
   * 迁移对应关系: Go语言DatasetServiceImpl.saveItemData
   * - 功能: 保存项目数据
   * - 参数: items - 项目列表
   * - 用途: 项目数据存储
   */
  private void saveItemData(Item... items) {
    // TODO: 实现项目数据存储逻辑
    // 根据项目大小选择合适的存储提供商
    for (Item item : items) {
      ItemDataProperties props = item.getOrBuildProperties();
      // 设置存储提供商和键
      if (props.getBytes() > 1024 * 1024) { // 大于1MB使用对象存储
        props.setStorage("S3");
        props.setStorageKey(ItemUtils.formatDatasetItemDataKey(item.getDatasetId(), item.getItemId(), item.getAddVN()));
      }
      else {
        props.setStorage("RDS");
      }
    }
  }
}
