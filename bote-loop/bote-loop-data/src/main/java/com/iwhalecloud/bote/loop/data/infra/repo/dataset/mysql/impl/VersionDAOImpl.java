package com.iwhalecloud.bote.loop.data.infra.repo.dataset.mysql.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.entity.loop.data.dataset.DatasetVersionEntity;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.dto.ListDatasetVersionsParams;
import com.iwhalecloud.bote.loop.data.infra.repo.dataset.mysql.VersionDAO;
import com.iwhalecloud.bote.mapper.loop.data.dataset.VersionMapper;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

/**
 * 数据集版本DAO实现类
 */
@Repository
@RequiredArgsConstructor
public class VersionDAOImpl implements VersionDAO {
  private final VersionMapper versionMapper;

  /**
   * 创建版本
   */
  @Override
  public void createVersion(DatasetVersionEntity version) {
    if (version == null) {
      throw new BssException("version is required");
    }
    versionMapper.createVersion(version);
  }

  /**
   * 获取版本
   */
  @Override
  public DatasetVersionEntity getVersion(Long spaceId, Long versionId) {
    if (spaceId == null || versionId == null) {
      throw new BssException("spaceId and versionId are required");
    }

    return versionMapper.getVersion(spaceId, versionId);
  }

  /**
   * 批量获取版本
   */
  @Override
  public List<DatasetVersionEntity> mGetVersions(Long spaceId, List<Long> ids) {
    if (spaceId == null) {
      throw new BssException("spaceId is required");
    }
    if (CollectionUtils.isEmpty(ids)) {
      return new java.util.ArrayList<>();
    }
    return versionMapper.mGetVersions(spaceId, ids);
  }

  /**
   * 列表查询版本
   */
  @Override
  public PageInfo<DatasetVersionEntity> listVersions(ListDatasetVersionsParams params) {
    if (params == null) {
      throw new BssException("params is required");
    }
    RowBounds rowBounds = params.buildRowBounds();
    return versionMapper.listVersions(params, rowBounds).toPageInfo();
  }

  /**
   * 统计版本数量
   */
  @Override
  public Long countVersions(ListDatasetVersionsParams params) {
    if (params == null) {
      throw new BssException("params is required");
    }

    Long count = versionMapper.countVersions(params);
    return count != null ? count : 0L;
  }

  /**
   * 更新版本
   */
  @Override
  public void patchVersion(DatasetVersionEntity patch, DatasetVersionEntity where) {
    if (patch == null || where == null) {
      throw new BssException("patch and where are required");
    }
    if (where.getId() == null || where.getId() <= 0 || where.getSpaceId() == null) {
      throw new BssException("both version_id and space_id are required");
    }

    // 如果patch的updateVersion为0，则设置为where的updateVersion + 1
    if (patch.getUpdateVersion() == null || patch.getUpdateVersion() == 0) {
      patch.setUpdateVersion(where.getUpdateVersion() + 1);
    }
    versionMapper.patchVersion(patch, where);
  }
}
