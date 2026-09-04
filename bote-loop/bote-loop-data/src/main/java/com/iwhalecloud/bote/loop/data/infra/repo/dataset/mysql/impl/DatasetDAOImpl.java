package com.iwhalecloud.bote.loop.data.infra.repo.dataset.mysql.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.entity.loop.data.dataset.DatasetEntity;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.dto.ListDatasetsParams;
import com.iwhalecloud.bote.loop.data.infra.repo.dataset.mysql.DatasetDAO;
import com.iwhalecloud.bote.mapper.loop.data.dataset.DatasetMapper;
import com.iwhalecloud.bss.litchi.base.exception.BssException;

import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

/**
 * 数据集DAO实现类
 */
@Repository
@RequiredArgsConstructor
public class DatasetDAOImpl implements DatasetDAO {
  private final DatasetMapper datasetMapper;

  /**
   * 创建数据集
   */
  @Override
  public void createDataset(DatasetEntity dataset) {
    if (dataset == null) {
      throw new BssException("dataset is required");
    }
    datasetMapper.createDataset(dataset);
  }

  /**
   * 更新数据集
   */
  @Override
  public void patchDataset(DatasetEntity patch, DatasetEntity where) {
    if (patch == null || where == null) {
      throw new BssException("patch and where are required");
    }
    if (where.getId() == null || where.getSpaceId() == null) {
      throw new BssException("both dataset_id and space_id are required");
    }
    datasetMapper.patchDataset(patch, where);
  }

  /**
   * 删除数据集
   */
  @Override
  public void deleteDataset(Long spaceId, Long datasetId) {
    if (spaceId == null || datasetId == null) {
      throw new BssException("spaceId and datasetId are required");
    }
    // 计算当前时间戳（秒）
    long deletedAt = System.currentTimeMillis() / 1000;
    datasetMapper.deleteDataset(spaceId, datasetId, deletedAt);
  }

  /**
   * 获取数据集
   */
  @Override
  public DatasetEntity getDataset(Long spaceId, Long datasetId) {
    if (spaceId == null || datasetId == null) {
      throw new BssException("spaceId and datasetId are required");
    }

    return datasetMapper.getDataset(spaceId, datasetId);
  }

  /**
   * 批量获取数据集
   */
  @Override
  public List<DatasetEntity> mGetDatasets(Long spaceId, List<Long> ids) {
    if (spaceId == null) {
      throw new BssException("spaceId is required");
    }
    if (CollectionUtils.isEmpty(ids)) {
      return Collections.emptyList();
    }
    return datasetMapper.mGetDatasets(spaceId, ids);
  }

  /**
   * 列表查询数据集
   */
  @Override
  public PageInfo<DatasetEntity> listDatasets(ListDatasetsParams params) {
    RowBounds rowBounds = params.buildRowBounds();
    return datasetMapper.listDatasets(params, rowBounds).toPageInfo();
  }

  /**
   * 统计数据集数量
   */
  @Override
  public Long countDatasets(ListDatasetsParams params) {
    if (params == null) {
      throw new BssException("params is required");
    }

    Long count = datasetMapper.countDatasets(params);
    return count != null ? count : 0L;
  }
}
