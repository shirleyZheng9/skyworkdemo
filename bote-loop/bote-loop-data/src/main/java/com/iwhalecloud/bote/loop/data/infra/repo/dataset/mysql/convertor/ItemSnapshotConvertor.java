package com.iwhalecloud.bote.loop.data.infra.repo.dataset.mysql.convertor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.iwhalecloud.bote.entity.loop.data.dataset.DatasetItemSnapshotEntity;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.FieldData;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.Item;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.ItemData;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.ItemDataProperties;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.ItemSnapshot;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.List;
import java.util.Optional;

/**
 * 项目快照转换器
 * 迁移对应关系: Go语言convertor包中的ItemSnapshot转换器
 * - 功能: 提供项目快照DO和PO之间的转换
 * - 方法定义: 各种项目快照转换方法
 * <p>
 * Java实现说明:
 * - 对应Go的convertor包中的ItemSnapshot转换器
 * - 使用Java静态方法提供转换功能
 * - 提供项目快照DO和PO之间的转换
 * <p>
 * 技术栈迁移:
 * - Go方法 -> Java静态方法
 * - Go JSON序列化 -> Jackson序列化
 * - Go错误处理 -> Java异常处理
 */
public final class ItemSnapshotConvertor {

  private ItemSnapshotConvertor() {
  }

  private static final ObjectMapper objectMapper = JsonMapper.builder().build();

  /**
   * 将ItemSnapshot DO转换为PO
   * 迁移对应关系: Go语言ItemSnapshotDO2PO
   * - 功能: 将项目快照DO转换为PO
   * - 参数: do_ - 项目快照DO对象
   * - 返回: 项目快照PO对象
   * - 用途: 数据持久化前的转换
   */
  public static DatasetItemSnapshotEntity itemSnapshotDO2PO(ItemSnapshot itemSnapshot) {
    if (itemSnapshot == null || itemSnapshot.getSnapshot() == null) {
      return null;
    }

    Item snapshot = itemSnapshot.getSnapshot();
    DatasetItemSnapshotEntity.DatasetItemSnapshotEntityBuilder builder = buildBasicSnapshotEntity(itemSnapshot, snapshot);
    serializeSnapshotData(snapshot, builder);
    serializeRepeatedData(snapshot, builder);
    serializeDataProperties(snapshot, builder);

    return builder.build();
  }

  private static DatasetItemSnapshotEntity.DatasetItemSnapshotEntityBuilder buildBasicSnapshotEntity(ItemSnapshot itemSnapshot, Item snapshot) {
    // TODO appId默认值0
    return DatasetItemSnapshotEntity.builder()
      .id(itemSnapshot.getId())
      .appId(0)
      .spaceId(snapshot.getSpaceId())
      .datasetId(snapshot.getDatasetId())
      .schemaId(snapshot.getSchemaId())
      .versionId(itemSnapshot.getVersionId())
      .itemPrimaryId(snapshot.getId())
      .itemId(snapshot.getItemId())
      .itemKey(snapshot.getItemKey())
      .addVn(snapshot.getAddVN())
      .delVn(snapshot.getDelVN())
      .createdAt(itemSnapshot.getCreatedAt())
      .itemCreatedBy(snapshot.getCreatedBy())
      .itemCreatedAt(snapshot.getCreatedAt())
      .itemUpdatedBy(Optional.ofNullable(snapshot.getUpdatedBy()).orElse("1"))
      .itemUpdatedAt(snapshot.getUpdatedAt());
  }

  private static void serializeSnapshotData(Item snapshot, DatasetItemSnapshotEntity.DatasetItemSnapshotEntityBuilder builder) {
    if (snapshot.getData() != null && !snapshot.getData().isEmpty()) {
      try {
        String dataJson = objectMapper.writeValueAsString(snapshot.getData());
        builder.data(dataJson);
      }
      catch (Exception e) {
        throw new BssException("marshal data of snapshot failed", e);
      }
    }
  }

  private static void serializeRepeatedData(Item snapshot, DatasetItemSnapshotEntity.DatasetItemSnapshotEntityBuilder builder) {
    if (snapshot.getRepeatedData() != null && !snapshot.getRepeatedData().isEmpty()) {
      try {
        String repeatedDataJson = objectMapper.writeValueAsString(snapshot.getRepeatedData());
        builder.repeatedData(repeatedDataJson);
      }
      catch (Exception e) {
        throw new BssException("marshal repeated data of snapshot failed", e);
      }
    }
  }

  private static void serializeDataProperties(Item snapshot, DatasetItemSnapshotEntity.DatasetItemSnapshotEntityBuilder builder) {
    if (snapshot.getDataProperties() != null) {
      try {
        String dataPropertiesJson = objectMapper.writeValueAsString(snapshot.getDataProperties());
        builder.dataProperties(dataPropertiesJson);
      }
      catch (Exception e) {
        throw new BssException("marshal data properties of snapshot failed", e);
      }
    }
  }

  /**
   * 将ItemSnapshot PO转换为DO
   * 迁移对应关系: Go语言ConvertItemSnapshotPO2DO
   * - 功能: 将项目快照PO转换为DO
   * - 参数: po - 项目快照PO对象
   * - 返回: 项目快照DO对象
   * - 用途: 数据查询后的转换
   */
  public static ItemSnapshot itemSnapshotPO2DO(DatasetItemSnapshotEntity po) {
    if (po == null) {
      return null;
    }

    ItemSnapshot.ItemSnapshotBuilder builder = buildBasicItemSnapshot(po);
    deserializeSnapshotData(po, builder);
    deserializeRepeatedData(po, builder);
    deserializeDataProperties(po, builder);

    return builder.build();
  }

  private static ItemSnapshot.ItemSnapshotBuilder buildBasicItemSnapshot(DatasetItemSnapshotEntity po) {
    return ItemSnapshot.builder()
      .id(po.getId())
      .versionId(po.getVersionId())
      .createdAt(po.getCreatedAt())
      .snapshot(createBasicItem(po));
  }

  private static Item createBasicItem(DatasetItemSnapshotEntity po) {
    return Item.builder()
      .id(po.getItemPrimaryId())
      .appId(po.getAppId())
      .spaceId(po.getSpaceId())
      .datasetId(po.getDatasetId())
      .schemaId(po.getSchemaId())
      .itemId(po.getItemId())
      .itemKey(po.getItemKey())
      .addVN(po.getAddVn())
      .delVN(po.getDelVn())
      .createdBy(po.getItemCreatedBy())
      .createdAt(po.getItemCreatedAt())
      .updatedBy(po.getItemUpdatedBy())
      .updatedAt(po.getItemUpdatedAt())
      .build();
  }

  private static void deserializeSnapshotData(DatasetItemSnapshotEntity po, ItemSnapshot.ItemSnapshotBuilder builder) {
    if (po.getData() != null && !po.getData().isEmpty()) {
      try {
        List<FieldData> data = objectMapper.readValue(po.getData(), new TypeReference<>() {
        });
        Item currentSnapshot = builder.build().getSnapshot();
        builder.snapshot(createItemWithData(currentSnapshot, data, null, null));
      }
      catch (Exception e) {
        throw new BssException("unmarshal data of snapshot failed", e);
      }
    }
  }

  private static void deserializeRepeatedData(DatasetItemSnapshotEntity po, ItemSnapshot.ItemSnapshotBuilder builder) {
    if (po.getRepeatedData() != null && !po.getRepeatedData().isEmpty()) {
      try {
        List<ItemData> repeatedData = objectMapper.readValue(po.getRepeatedData(), new TypeReference<>() {
        });
        Item currentSnapshot = builder.build().getSnapshot();
        builder.snapshot(createItemWithData(currentSnapshot, currentSnapshot.getData(), repeatedData, null));
      }
      catch (Exception e) {
        throw new BssException("unmarshal repeated data of snapshot failed", e);
      }
    }
  }

  private static void deserializeDataProperties(DatasetItemSnapshotEntity po, ItemSnapshot.ItemSnapshotBuilder builder) {
    if (po.getDataProperties() != null && !po.getDataProperties().isEmpty()) {
      try {
        ItemDataProperties dataProperties = objectMapper.readValue(po.getDataProperties(), ItemDataProperties.class);
        Item currentSnapshot = builder.build().getSnapshot();
        builder.snapshot(createItemWithData(currentSnapshot, currentSnapshot.getData(), currentSnapshot.getRepeatedData(), dataProperties));
      }
      catch (Exception e) {
        throw new BssException("unmarshal data properties of snapshot failed", e);
      }
    }
  }

  private static Item createItemWithData(Item currentSnapshot, List<FieldData> data, List<ItemData> repeatedData, ItemDataProperties dataProperties) {
    return Item.builder()
      .id(currentSnapshot.getId())
      .appId(currentSnapshot.getAppId())
      .spaceId(currentSnapshot.getSpaceId())
      .datasetId(currentSnapshot.getDatasetId())
      .schemaId(currentSnapshot.getSchemaId())
      .itemId(currentSnapshot.getItemId())
      .itemKey(currentSnapshot.getItemKey())
      .addVN(currentSnapshot.getAddVN())
      .delVN(currentSnapshot.getDelVN())
      .createdBy(currentSnapshot.getCreatedBy())
      .createdAt(currentSnapshot.getCreatedAt())
      .updatedBy(currentSnapshot.getUpdatedBy())
      .updatedAt(currentSnapshot.getUpdatedAt())
      .data(data)
      .repeatedData(repeatedData)
      .dataProperties(dataProperties)
      .build();
  }
}
