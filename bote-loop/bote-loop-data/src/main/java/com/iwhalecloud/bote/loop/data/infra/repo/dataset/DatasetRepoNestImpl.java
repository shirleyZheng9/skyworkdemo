package com.iwhalecloud.bote.loop.data.infra.repo.dataset;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.entity.loop.data.dataset.DatasetEntity;
import com.iwhalecloud.bote.entity.loop.data.dataset.DatasetSchemaEntity;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.Dataset;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetSchema;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.FieldSchema;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.IDatasetRepo;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.dto.ListDatasetsParams;
import com.iwhalecloud.bote.loop.data.infra.repo.dataset.mysql.DatasetDAO;
import com.iwhalecloud.bote.loop.data.infra.repo.dataset.mysql.SchemaDAO;
import com.iwhalecloud.bote.loop.data.infra.repo.dataset.mysql.convertor.DatasetConvertor;
import com.iwhalecloud.bote.loop.data.infra.repo.dataset.mysql.convertor.SchemaConvertor;
import com.iwhalecloud.bote.loop.data.infra.repo.dataset.redis.RedisDatasetDAO;
import com.iwhalecloud.bote.loop.infra.idgen.IIDGenerator;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 数据集仓库实现类
 */
@Component("datasetRepoNestImpl")
@RequiredArgsConstructor
public class DatasetRepoNestImpl implements IDatasetRepo {
  private final DatasetDAO datasetDAO;
  private final SchemaDAO schemaDAO;
  private final RedisDatasetDAO datasetRedisDAO;
  private final IIDGenerator idGenerator;

  /**
   * 设置数据集项目数量
   */
  @Override
  public void setItemCount(Long datasetID, Long n) {
    datasetRedisDAO.setItemCount(datasetID, n);
  }

  /**
   * 增加数据集项目数量
   */
  @Override
  public Long incrItemCount(Long datasetID, Long n) {
    return datasetRedisDAO.incrItemCount(datasetID, n);
  }

  /**
   * 获取数据集项目数量
   */
  @Override
  public Long getItemCount(Long datasetID) {
    return datasetRedisDAO.getItemCount(datasetID);
  }

  /**
   * 批量获取数据集项目数量
   */
  @Override
  public Map<Long, Long> mGetItemCount(List<Long> datasetIDs) {
    return datasetRedisDAO.mGetItemCount(datasetIDs);
  }

  /**
   * 创建数据集和模式
   */
  @Override
  @Transactional
  public void createDatasetAndSchema(Dataset dataset, List<FieldSchema> fields) {
    // 生成ID
    dataset.setId(idGenerator.genId());
    dataset.setSchemaId(idGenerator.genId());

    // 插入数据集
    DatasetEntity datasetPO = DatasetConvertor.datasetDO2PO(dataset);
    datasetDAO.createDataset(datasetPO);
    dataset.setId(datasetPO.getId());
    dataset.setCreatedAt(datasetPO.getCreatedAt());
    dataset.setUpdatedAt(datasetPO.getUpdatedAt());

    // 插入模式
    DatasetSchema schema = newSchemaOfDataset(dataset, fields);
    DatasetSchemaEntity schemaPO = SchemaConvertor.schemaDO2PO(schema);
    schemaDAO.createSchema(schemaPO);
    schema.setId(schemaPO.getId());
    schema.setCreatedAt(schemaPO.getCreatedAt());

//            if (!idGenOk) {
//                // ID生成失败，更新数据集的schemaID
//                logger.info("previous idgen failed, update dataset schemaID to {}", schema.getId());
//                DatasetEntity patch = DatasetEntity.builder()
//                        .schemaId(schema.getId())
//                        .updatedBy(SessionContext.getCurrentUserId())
//                        .build();
//                DatasetEntity where = DatasetEntity.builder()
//                        .spaceId(dataset.getSpaceId())
//                        .id(dataset.getId())
//                        .build();
//                datasetDAO.patchDataset(patch, where);
//            }
  }

  /**
   * 获取数据集
   */
  @Override
  public Dataset getDataset(Long spaceID, Long id) {
    DatasetEntity po = datasetDAO.getDataset(spaceID, id);
    if (po == null) {
      return null;
    }
    return DatasetConvertor.datasetPO2DO(po);
  }

  /**
   * 批量获取数据集
   */
  @Override
  public List<Dataset> mGetDatasets(Long spaceID, List<Long> ids) {
    List<DatasetEntity> pos = datasetDAO.mGetDatasets(spaceID, ids);
    if (pos == null || pos.isEmpty()) {
      return List.of();
    }
    return pos.stream()
      .map(DatasetConvertor::datasetPO2DO)
      .collect(Collectors.toList());
  }

  /**
   * 更新数据集
   */
  @Override
  public void patchDataset(Dataset patch, Dataset where) {
    DatasetEntity patchPO = DatasetConvertor.datasetDO2PO(patch, false);
    DatasetEntity wherePO = DatasetConvertor.datasetDO2PO(where);
    datasetDAO.patchDataset(patchPO, wherePO);
  }

  /**
   * 删除数据集
   */
  @Override
  public void deleteDataset(Long spaceID, Long id) {
    datasetDAO.deleteDataset(spaceID, id);
  }

  /**
   * 列表查询数据集
   */
  @Override
  public PageInfo<Dataset> listDatasets(ListDatasetsParams params) {
    PageInfo<DatasetEntity> pageInfo = datasetDAO.listDatasets(params);
    return pageInfo.convert(DatasetConvertor::datasetPO2DO);
  }

  /**
   * 统计数据集数量
   */
  @Override
  public Long countDatasets(ListDatasetsParams params) {
    return datasetDAO.countDatasets(params);
  }

  /**
   * 创建数据集模式
   */
  private DatasetSchema newSchemaOfDataset(Dataset dataset, List<FieldSchema> fields) {
    boolean immutable = false;
    if (dataset.getFeatures() != null) {
      immutable = !dataset.getFeatures().getEditSchema();
    }

    return DatasetSchema.builder()
      .id(dataset.getSchemaId())
      .appId(dataset.getAppId())
      .spaceId(dataset.getSpaceId())
      .datasetId(dataset.getId())
      .fields(fields)
      .immutable(immutable)
      .createdBy(dataset.getCreatedBy())
      .updatedBy(dataset.getCreatedBy())
      .build();
  }
}
