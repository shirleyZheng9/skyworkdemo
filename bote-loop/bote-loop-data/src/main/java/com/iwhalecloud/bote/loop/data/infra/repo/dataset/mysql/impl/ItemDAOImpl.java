package com.iwhalecloud.bote.loop.data.infra.repo.dataset.mysql.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.entity.loop.data.dataset.DatasetItemEntity;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.ItemIdentity;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.dto.ListItemsParams;
import com.iwhalecloud.bote.loop.data.infra.repo.dataset.mysql.ItemDAO;
import com.iwhalecloud.bote.loop.data.pkg.pagination.Paginator;
import com.iwhalecloud.bote.mapper.loop.data.dataset.ItemMapper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

/**
 * 数据集项目DAO实现类
 */
@Repository
@RequiredArgsConstructor
public class ItemDAOImpl implements ItemDAO {
  private final ItemMapper itemMapper;

  /**
   * 统计项目数量
   */
  @Override
  public Long countItems(ListItemsParams params) {
    Assert.notNull(params, "params is null");
    return itemMapper.countByCondition(params);
  }

  /**
   * 批量创建项目
   */
  @Override
  @Transactional
  public Long mCreateItems(List<DatasetItemEntity> items) {
    if (CollectionUtils.isEmpty(items)) {
      return 0L;
    }

    Set<Long> existingItemIds = findExistingItemIds(items);
    ItemsPartition partition = partitionItems(items, existingItemIds);
    return executeInsertAndUpdate(partition);
  }

  /**
   * 查找已存在的项目ID
   */
  private Set<Long> findExistingItemIds(List<DatasetItemEntity> items) {
    List<Long> itemIds = items.stream()
      .map(DatasetItemEntity::getItemId)
      .filter(itemId -> itemId != null && itemId > 0)
      .distinct()
      .toList();

    if (itemIds.isEmpty() || items.isEmpty()) {
      return new HashSet<>();
    }

    DatasetItemEntity firstItem = items.get(0);
    ListItemsParams queryParams = ListItemsParams.builder()
      .spaceId(firstItem.getSpaceId())
      .datasetId(firstItem.getDatasetId())
      .itemIds(itemIds)
      .build();
    List<DatasetItemEntity> existingItems = itemMapper.selectByCondition(queryParams);
    return existingItems.stream()
      .map(DatasetItemEntity::getItemId)
      .collect(Collectors.toSet());
  }

  /**
   * 分离需要插入和更新的项目
   */
  private ItemsPartition partitionItems(List<DatasetItemEntity> items, Set<Long> existingItemIds) {
    List<DatasetItemEntity> itemsToInsert = new ArrayList<>();
    List<DatasetItemEntity> itemsToUpdate = new ArrayList<>();

    for (DatasetItemEntity item : items) {
      if (item.getUpdateVersion() == null) {
        item.setUpdateVersion(0L);
      }

      if (existingItemIds.contains(item.getItemId())) {
        itemsToUpdate.add(item);
      } else {
        itemsToInsert.add(item);
      }
    }

    return new ItemsPartition(itemsToInsert, itemsToUpdate);
  }

  /**
   * 执行插入和更新操作
   */
  private Long executeInsertAndUpdate(ItemsPartition partition) {
    int insertCount = 0;
    if (!partition.getItemsToInsert().isEmpty()) {
      insertCount = itemMapper.batchInsertItems(partition.getItemsToInsert());
    }

    for (DatasetItemEntity item : partition.getItemsToUpdate()) {
      itemMapper.updateItem(item);
    }

    return (long) insertCount;
  }

  /**
   * 列表查询项目
   */
  @Override
  public PageInfo<DatasetItemEntity> listItems(ListItemsParams params) {
    Assert.notNull(params, "params is null");
    Paginator paginator = params.getPaginator();
    if (paginator != null && paginator.getPageNum() != null && paginator.getPageSize() != null) {
      RowBounds rowBounds = paginator.buildRowBounds();
      return itemMapper.selectByCondition(params, rowBounds).toPageInfo();
    }
    return new PageInfo<>(itemMapper.selectByCondition(params));
  }

  /**
   * 更新项目
   */
  @Override
  public void updateItem(DatasetItemEntity item) {
    Assert.notNull(item, "item is null");
    itemMapper.updateBySpaceIdAndIdAndAddVnAndDelVn(item);
  }

  /**
   * 删除项目
   */
  @Override
  public void deleteItems(Long spaceID, List<Long> ids) {
    Assert.notNull(spaceID, "spaceID is required");
    if (CollectionUtils.isEmpty(ids)) {
      return;
    }
    Long deletedAt = System.currentTimeMillis();
    itemMapper.deleteBySpaceIdAndIds(spaceID, ids, deletedAt);
  }

  /**
   * 归档项目
   */
  @Override
  public void archiveItems(Long spaceID, Long delVN, List<Long> ids) {
    Assert.notNull(spaceID, "spaceID is required");
    Assert.notNull(delVN, "delVN is required");
    Assert.state(delVN > 0, "delVN is required");
    if (CollectionUtils.isEmpty(ids)) {
      return;
    }
    itemMapper.updateDelVnBySpaceIdAndIds(spaceID, delVN, ids);
  }

  /**
   * 清空数据集
   */
  @Override
  @Transactional
  public List<ItemIdentity> clearDataset(Long spaceID, Long datasetID, Long delVN) {
    Assert.notNull(spaceID, "spaceID is required");
    Assert.notNull(datasetID, "datasetID is required");
    Assert.state(datasetID > 0, "datasetID is required");
    Assert.notNull(delVN, "delVN is required");
    Assert.state(delVN > 0, "delVN is required");
    return processClearDatasetBatch(spaceID, datasetID, delVN);
  }

  private List<ItemIdentity> processClearDatasetBatch(Long spaceID, Long datasetID, Long delVN) {
    List<ItemIdentity> result = new ArrayList<>();
    int batchSize = 1000;
    int offset = 0;

    while (true) {
      RowBounds rowBounds = new RowBounds(offset, batchSize);
      List<DatasetItemEntity> items = itemMapper.selectBySpaceIdAndDatasetIdAndDelVnForClear(
        spaceID, datasetID, Long.MAX_VALUE, rowBounds);

      if (CollectionUtils.isEmpty(items)) {
        break;
      }

      processBatchItems(spaceID, datasetID, delVN, items, result);
      offset += batchSize;
    }

    return result;
  }

  private void processBatchItems(Long spaceID, Long datasetID, Long delVN,
                                 List<DatasetItemEntity> items, List<ItemIdentity> result) {
    List<ItemIdentity> toDelete = new ArrayList<>();
    List<ItemIdentity> toUpdate = new ArrayList<>();

    for (DatasetItemEntity item : items) {
      ItemIdentity identity = createItemIdentity(spaceID, datasetID, item);
      if (item.getAddVn().equals(delVN)) {
        toDelete.add(identity);
      }
      else {
        toUpdate.add(identity);
      }
    }

    deleteItems(spaceID, toDelete, result);
    updateItems(spaceID, delVN, toUpdate, result);
  }

  private ItemIdentity createItemIdentity(Long spaceID, Long datasetID, DatasetItemEntity item) {
    return ItemIdentity.builder()
      .spaceId(spaceID)
      .datasetId(datasetID)
      .id(item.getId())
      .itemId(item.getItemId())
      .addVN(item.getAddVn())
      .build();
  }

  private void deleteItems(Long spaceID, List<ItemIdentity> toDelete, List<ItemIdentity> result) {
    if (!toDelete.isEmpty()) {
      List<Long> deleteIds = toDelete.stream()
        .map(ItemIdentity::getId)
        .collect(Collectors.toList());
      Long deletedAt = System.currentTimeMillis();
      itemMapper.deleteBySpaceIdAndIds(spaceID, deleteIds, deletedAt);
      result.addAll(toDelete);
    }
  }

  private void updateItems(Long spaceID, Long delVN, List<ItemIdentity> toUpdate, List<ItemIdentity> result) {
    if (!toUpdate.isEmpty()) {
      List<Long> updateIds = toUpdate.stream()
        .map(ItemIdentity::getId)
        .collect(Collectors.toList());
      itemMapper.updateDelVnBySpaceIdAndIds(spaceID, delVN, updateIds);
      result.addAll(toUpdate);
    }
  }

  /**
   * 项目分区容器类
   */
  private static class ItemsPartition {
    private final List<DatasetItemEntity> itemsToInsert;
    private final List<DatasetItemEntity> itemsToUpdate;

    public ItemsPartition(List<DatasetItemEntity> itemsToInsert, List<DatasetItemEntity> itemsToUpdate) {
      this.itemsToInsert = itemsToInsert;
      this.itemsToUpdate = itemsToUpdate;
    }

    public List<DatasetItemEntity> getItemsToInsert() {
      return itemsToInsert;
    }

    public List<DatasetItemEntity> getItemsToUpdate() {
      return itemsToUpdate;
    }
  }
}
