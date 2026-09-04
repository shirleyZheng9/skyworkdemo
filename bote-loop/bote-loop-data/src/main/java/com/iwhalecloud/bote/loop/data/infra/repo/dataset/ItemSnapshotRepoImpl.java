package com.iwhalecloud.bote.loop.data.infra.repo.dataset;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.entity.loop.data.dataset.DatasetItemSnapshotEntity;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.ItemSnapshot;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.IItemSnapshotRepo;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.dto.ListItemSnapshotsParams;
import com.iwhalecloud.bote.loop.data.infra.repo.dataset.mysql.ItemSnapshotDAO;
import com.iwhalecloud.bote.loop.data.infra.repo.dataset.mysql.convertor.ItemSnapshotConvertor;
import com.iwhalecloud.bote.loop.infra.idgen.IIDGenerator;
import org.springframework.util.Assert;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据集项目快照Repository实现类
 */
@Component("itemSnapshotRepoImpl")
@RequiredArgsConstructor
public class ItemSnapshotRepoImpl implements IItemSnapshotRepo {
  private final ItemSnapshotDAO itemSnapshotDAO;
  private final IIDGenerator idGenerator;

  /**
   * 批量更新插入项目快照
   */
  @Override
  public Long batchUpsertItemSnapshots(List<ItemSnapshot> snapshots) {
    if (CollectionUtils.isEmpty(snapshots)) {
      return 0L;
    }
    // 生成ID
    for (ItemSnapshot snapshot : snapshots) {
      if (snapshot.getId() == null || snapshot.getId() == 0) {
        snapshot.setId(idGenerator.genId());
      }
    }
    // 转换为PO
    List<DatasetItemSnapshotEntity> pos = snapshots.stream()
      .map(ItemSnapshotConvertor::itemSnapshotDO2PO)
      .collect(Collectors.toList());
    // 批量更新插入
    return itemSnapshotDAO.batchUpsertItemSnapshots(pos);
  }

  /**
   * 列表查询项目快照
   */
  @Override
  public PageInfo<ItemSnapshot> listItemSnapshots(ListItemSnapshotsParams params) {
    Assert.notNull(params, "params is null");
    // 构建DAO参数
    ListItemSnapshotsParams daoParam = new ListItemSnapshotsParams();
    daoParam.setPaginator(params.getPaginator());
    daoParam.setSpaceId(params.getSpaceId());
    daoParam.setVersionId(params.getVersionId());
    // 查询数据
    PageInfo<DatasetItemSnapshotEntity> pos = itemSnapshotDAO.listItemSnapshots(daoParam);
    return pos.convert(ItemSnapshotConvertor::itemSnapshotPO2DO);
  }

  /**
   * 统计项目快照数量
   */
  @Override
  public Long countItemSnapshots(ListItemSnapshotsParams params) {
    Assert.notNull(params, "params is null");
    // 构建DAO参数
    ListItemSnapshotsParams daoParam = new ListItemSnapshotsParams();
    daoParam.setPaginator(params.getPaginator());
    daoParam.setSpaceId(params.getSpaceId());
    daoParam.setVersionId(params.getVersionId());
    // 统计数量
    return itemSnapshotDAO.countItemSnapshots(daoParam);
  }
}
