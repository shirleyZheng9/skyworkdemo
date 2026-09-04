package com.iwhalecloud.bote.loop.data.infra.repo.dataset;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.entity.loop.data.dataset.DatasetVersionEntity;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetVersion;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.IVersionRepo;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.dto.ListDatasetVersionsParams;
import com.iwhalecloud.bote.loop.data.infra.repo.dataset.mysql.VersionDAO;
import com.iwhalecloud.bote.loop.data.infra.repo.dataset.mysql.convertor.VersionConvertor;
import com.iwhalecloud.bote.loop.data.infra.repo.dataset.redis.RedisVersionDAO;
import com.iwhalecloud.bote.loop.infra.idgen.IIDGenerator;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.util.Assert;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * 数据集版本Repository实现类
 */
@Component("versionRepoImpl")
@RequiredArgsConstructor
public class VersionRepoImpl implements IVersionRepo {
  private final VersionDAO versionDAO;
  private final RedisVersionDAO redisVersionDAO;
  private final IIDGenerator idGenerator;

  /**
   * 创建版本
   */
  @Override
  public void createVersion(DatasetVersion version) {
    Assert.notNull(version, "version is null");
    // 生成ID
    if (version.getId() == null || version.getId() == 0) {
      version.setId(idGenerator.genId());
    }
    // 转换为PO
    DatasetVersionEntity versionPO = VersionConvertor.versionDO2PO(version);
    // 创建版本
    versionDAO.createVersion(versionPO);
    // 更新ID和创建时间
    version.setId(versionPO.getId());
    version.setCreatedAt(versionPO.getCreatedAt());
  }

  /**
   * 获取版本
   */
  @Override
  public DatasetVersion getVersion(Long spaceID, Long versionID) {
    if (spaceID == null) {
      throw new BssException("spaceID is required");
    }
    if (versionID == null) {
      throw new BssException("versionID is required");
    }

    DatasetVersionEntity versionPO = versionDAO.getVersion(spaceID, versionID);
    if (versionPO == null) {
      return null;
    }
    return VersionConvertor.versionPO2DO(versionPO);
  }

  /**
   * 批量获取版本
   */
  @Override
  public List<DatasetVersion> mGetVersions(Long spaceID, List<Long> ids) {
    if (spaceID == null) {
      throw new BssException("spaceID is required");
    }
    if (CollectionUtils.isEmpty(ids)) {
      return List.of();
    }

    List<DatasetVersionEntity> versionPOs = versionDAO.mGetVersions(spaceID, ids);
    if (CollectionUtils.isEmpty(versionPOs)) {
      return List.of();
    }
    return versionPOs.stream()
      .map(VersionConvertor::versionPO2DO)
      .collect(Collectors.toList());
  }

  /**
   * 获取版本的项目数量
   */
  @Override
  public Long getItemCountOfVersion(Long versionID) {
    if (versionID == null || versionID <= 0) {
      throw new BssException("versionID is required");
    }

    return redisVersionDAO.getItemCountOfVersion(versionID);
  }

  /**
   * 设置版本的项目数量
   */
  @Override
  public void setItemCountOfVersion(Long datasetID, Long n) {
    if (datasetID == null || datasetID <= 0) {
      throw new BssException("datasetID is required");
    }
    if (n == null || n < 0) {
      throw new BssException("n is required and must be >= 0");
    }

    redisVersionDAO.setItemCountOfVersion(datasetID, n);
  }

  /**
   * 列表查询版本
   */
  @Override
  public PageInfo<DatasetVersion> listVersions(ListDatasetVersionsParams params) {
    if (params == null) {
      throw new BssException("params is null");
    }
    // 构建DAO参数
    ListDatasetVersionsParams daoParam = new ListDatasetVersionsParams();
    daoParam.setPaginator(params.getPaginator());
    daoParam.setSpaceId(params.getSpaceId());
    daoParam.setDatasetId(params.getDatasetId());
    daoParam.setIds(params.getIds());
    daoParam.setVersions(params.getVersions());
    daoParam.setVersionNums(params.getVersionNums());
    daoParam.setVersionLike(params.getVersionLike());
    // 查询数据
    PageInfo<DatasetVersionEntity> pos = versionDAO.listVersions(daoParam);
    return pos.convert(VersionConvertor::versionPO2DO);
  }

  /**
   * 统计版本数量
   */
  @Override
  public Long countVersions(ListDatasetVersionsParams params) {
    if (params == null) {
      throw new BssException("params is null");
    }
    // 构建DAO参数
    ListDatasetVersionsParams daoParam = new ListDatasetVersionsParams();
    daoParam.setPaginator(params.getPaginator());
    daoParam.setSpaceId(params.getSpaceId());
    daoParam.setDatasetId(params.getDatasetId());
    daoParam.setIds(params.getIds());
    daoParam.setVersions(params.getVersions());
    daoParam.setVersionNums(params.getVersionNums());
    daoParam.setVersionLike(params.getVersionLike());

    // 统计数量
    return versionDAO.countVersions(daoParam);
  }

  /**
   * 更新版本
   */
  @Override
  public void patchVersion(DatasetVersion patch, DatasetVersion where) {
    Assert.notNull(patch, "patch is null");
    Assert.notNull(where, "where is null");
    // 转换为PO
    DatasetVersionEntity patchPO = VersionConvertor.versionDO2PO(patch);
    DatasetVersionEntity wherePO = VersionConvertor.versionDO2PO(where);

    // 更新版本
    versionDAO.patchVersion(patchPO, wherePO);
  }
}
