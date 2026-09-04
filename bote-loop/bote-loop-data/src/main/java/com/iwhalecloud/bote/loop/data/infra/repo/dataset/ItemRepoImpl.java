package com.iwhalecloud.bote.loop.data.infra.repo.dataset;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.entity.loop.data.dataset.DatasetItemEntity;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.Item;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.ItemIdentity;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.IItemRepo;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.dto.ListItemsParams;
import com.iwhalecloud.bote.loop.data.infra.repo.dataset.mysql.ItemDAO;
import com.iwhalecloud.bote.loop.data.infra.repo.dataset.mysql.convertor.ItemConvertor;
import com.iwhalecloud.bote.loop.data.infra.repo.dataset.oss.OssItemDAO;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 项目仓库实现类
 */
@Component("itemRepoImpl")
@RequiredArgsConstructor
public class ItemRepoImpl implements IItemRepo {
  private final ItemDAO itemDAO;
  private final OssItemDAO ossItemDAO;

  /**
   * 统计项目数量
   */
  @Override
  public Long countItems(ListItemsParams params) {
    return itemDAO.countItems(params);
  }

  /**
   * 批量设置项目数据
   */
  @Override
  public Integer mSetItemData(List<Item> items) {
    return ossItemDAO.mSetItemData(items);
  }

  /**
   * 批量获取项目数据
   */
  @Override
  public void mGetItemData(List<Item> items) {
    ossItemDAO.mGetItemData(items);
    // 这里需要根据实际的DAO接口调用
    // dao.mGetItemData(items);
  }

  /**
   * 批量创建项目
   */
  @Override
  public Long mCreateItems(List<Item> items) {
    List<DatasetItemEntity> pos = items.stream()
      .map(ItemConvertor::itemDO2PO)
      .collect(Collectors.toList());
    itemDAO.mCreateItems(pos);
    return (long) pos.size();
  }

  /**
   * 列表查询项目
   */
  @Override
  public PageInfo<Item> listItems(ListItemsParams params) {
    PageInfo<DatasetItemEntity> items = itemDAO.listItems(params);
    return items.convert(ItemConvertor::itemPO2DO);
  }

  /**
   * 更新项目
   */
  @Override
  public void updateItem(Item item) {
    DatasetItemEntity po = ItemConvertor.itemDO2PO(item);
    itemDAO.updateItem(po);
  }

  /**
   * 删除项目
   */
  @Override
  public void deleteItems(Long spaceID, List<Long> ids) {
    itemDAO.deleteItems(spaceID, ids);
  }

  /**
   * 归档项目
   */
  @Override
  public void archiveItems(Long spaceID, Long delVN, List<Long> ids) {
    itemDAO.archiveItems(spaceID, delVN, ids);
  }

  /**
   * 清空数据集
   */
  @Override
  public List<ItemIdentity> clearDataset(Long spaceID, Long datasetID, Long delVN) {
    return itemDAO.clearDataset(spaceID, datasetID, delVN);
  }
}
