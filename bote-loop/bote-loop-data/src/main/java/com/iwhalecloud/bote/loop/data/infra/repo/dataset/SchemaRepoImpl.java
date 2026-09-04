package com.iwhalecloud.bote.loop.data.infra.repo.dataset;

import com.iwhalecloud.bote.entity.loop.data.dataset.DatasetSchemaEntity;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetSchema;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.ISchemaRepo;
import com.iwhalecloud.bote.loop.data.infra.repo.dataset.mysql.SchemaDAO;
import com.iwhalecloud.bote.loop.data.infra.repo.dataset.mysql.convertor.SchemaConvertor;
import com.iwhalecloud.bote.loop.infra.idgen.IIDGenerator;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * 数据集Schema Repository实现类
 */
@Component("schemaRepoImpl")
@RequiredArgsConstructor
public class SchemaRepoImpl implements ISchemaRepo {
  private final SchemaDAO schemaDAO;
  private final IIDGenerator idGenerator;

  /**
   * 获取Schema
   */
  @Override
  public DatasetSchema getSchema(Long spaceID, Long id) {
    if (spaceID == null) {
      throw new BssException("spaceID is required");
    }
    if (id == null) {
      throw new BssException("id is required");
    }
    DatasetSchemaEntity po = schemaDAO.getSchema(spaceID, id);
    if (po == null) {
      return null;
    }
    return SchemaConvertor.schemaPO2DO(po);
  }

  /**
   * 批量获取Schema
   */
  @Override
  public List<DatasetSchema> mGetSchema(Long spaceID, List<Long> ids) {
    if (spaceID == null) {
      throw new BssException("spaceID is required");
    }
    if (CollectionUtils.isEmpty(ids)) {
      return List.of();
    }
    List<DatasetSchemaEntity> pos = schemaDAO.mGetSchema(spaceID, ids);
    if (CollectionUtils.isEmpty(pos)) {
      return List.of();
    }
    return pos.stream()
      .map(SchemaConvertor::schemaPO2DO)
      .collect(Collectors.toList());
  }

  /**
   * 创建Schema
   */
  @Override
  public void createSchema(DatasetSchema schema) {
    if (schema == null) {
      throw new BssException("schema is null");
    }
    // 生成ID
    if (schema.getId() == null || schema.getId() == 0) {
      schema.setId(idGenerator.genId());
    }
    // 转换为PO
    DatasetSchemaEntity schemaPO = SchemaConvertor.schemaDO2PO(schema);
    // 创建Schema
    schemaDAO.createSchema(schemaPO);
    // 更新ID和创建时间
    schema.setId(schemaPO.getId());
    schema.setCreatedAt(schemaPO.getCreatedAt());
  }

  /**
   * 更新Schema
   */
  @Override
  public void updateSchema(Long updateVersion, DatasetSchema schema) {
    if (updateVersion == null || updateVersion <= 0) {
      throw new BssException("updateVersion is required");
    }
    if (schema == null) {
      throw new BssException("schema is null");
    }
    // 转换为PO
    DatasetSchemaEntity schemaPO = SchemaConvertor.schemaDO2PO(schema);
    // 更新Schema
    schemaDAO.updateSchema(updateVersion, schemaPO);
    // 更新更新时间
    schema.setUpdatedAt(schemaPO.getUpdatedAt());
  }
}
