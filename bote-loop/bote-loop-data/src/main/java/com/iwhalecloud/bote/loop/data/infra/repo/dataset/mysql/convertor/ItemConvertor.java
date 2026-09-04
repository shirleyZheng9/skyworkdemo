package com.iwhalecloud.bote.loop.data.infra.repo.dataset.mysql.convertor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.entity.loop.data.dataset.DatasetItemEntity;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.FieldData;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.Item;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.ItemData;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.ItemDataProperties;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.Date;
import java.util.List;

/**
 * 项目转换器
 * 迁移对应关系: Go语言convertor包中的Item转换器
 * - 功能: 提供项目DO和PO之间的转换
 * - 方法定义: 各种项目转换方法
 * <p>
 * Java实现说明:
 * - 对应Go的convertor包中的Item转换器
 * - 使用Java静态方法提供转换功能
 * - 提供项目DO和PO之间的转换
 * <p>
 * 技术栈迁移:
 * - Go方法 -> Java静态方法
 * - Go JSON序列化 -> Jackson序列化
 * - Go错误处理 -> Java异常处理
 */
public final class ItemConvertor {

  private ItemConvertor() {
    // 工具类，禁止实例化
  }

  /**
   * 将Item DO转换为PO
   */
  public static DatasetItemEntity itemDO2PO(Item item) {
    if (item == null) {
      return null;
    }

    // 转换 LocalDateTime 到 Date
    Date createdAt = item.getCreatedAt();
    Date updatedAt = item.getUpdatedAt();

    DatasetItemEntity.DatasetItemEntityBuilder builder = DatasetItemEntity.builder()
      .id(item.getId())
      .appId(item.getAppId())
      .spaceId(item.getSpaceId())
      .datasetId(item.getDatasetId())
      .schemaId(item.getSchemaId())
      .itemId(item.getItemId())
      .itemKey(item.getItemKey())
      .addVn(item.getAddVN())
      .delVn(item.getDelVN())
      .createdBy(item.getCreatedBy())
      .createdAt(createdAt)
      .updatedBy(item.getUpdatedBy())
      .updatedAt(updatedAt);

    // 处理数据
    if (item.getData() != null) {
      try {
        String dataJson = JsonUtil.toJsonString(item.getData());
        builder.data(dataJson);
      }
      catch (Exception e) {
        throw new BssException("marshal data failed", e);
      }
    }

    // 处理重复数据
    if (item.getRepeatedData() != null) {
      try {
        String repeatedDataJson = JsonUtil.toJsonString(item.getRepeatedData());
        builder.repeatedData(repeatedDataJson);
      }
      catch (Exception e) {
        throw new BssException("marshal repeated data failed", e);
      }
    }

    // 处理数据属性
    if (item.getDataProperties() != null) {
      try {
        String dataPropertiesJson = JsonUtil.toJsonString(item.getDataProperties());
        builder.dataProperties(dataPropertiesJson);
      }
      catch (Exception e) {
        throw new BssException("marshal data properties failed", e);
      }
    }

    return builder.build();
  }

  /**
   * 将Item PO转换为DO
   * 迁移对应关系: Go语言ItemPO2DO
   * - 功能: 将项目PO转换为DO
   * - 参数: po - 项目PO对象
   * - 返回: 项目DO对象
   * - 用途: 数据查询后的转换
   */
  public static Item itemPO2DO(DatasetItemEntity po) {
    if (po == null) {
      return null;
    }

    Item.ItemBuilder builder = buildBasicItem(po);
    setItemData(builder, po);
    setRepeatedData(builder, po);
    setDataProperties(builder, po);

    return builder.build();
  }

  private static Item.ItemBuilder buildBasicItem(DatasetItemEntity po) {
    return Item.builder()
      .id(po.getId())
      .appId(po.getAppId())
      .spaceId(po.getSpaceId())
      .datasetId(po.getDatasetId())
      .schemaId(po.getSchemaId())
      .itemId(po.getItemId())
      .itemKey(po.getItemKey())
      .addVN(po.getAddVn())
      .delVN(po.getDelVn())
      .createdBy(po.getCreatedBy())
      .createdAt(po.getCreatedAt())
      .updatedBy(po.getUpdatedBy())
      .updatedAt(po.getUpdatedAt());
  }

  private static void setItemData(Item.ItemBuilder builder, DatasetItemEntity po) {
    if (po.getData() != null && !po.getData().isEmpty()) {
      try {
        List<FieldData> data = JsonUtil.parseJson(po.getData(), new TypeReference<List<FieldData>>() {
        });
        builder.data(data);
      }
      catch (Exception e) {
        throw new BssException("unmarshal data failed", e);
      }
    }
  }

  private static void setRepeatedData(Item.ItemBuilder builder, DatasetItemEntity po) {
    if (po.getRepeatedData() != null && !po.getRepeatedData().isEmpty()) {
      try {
        List<ItemData> repeatedData = JsonUtil.parseJson(po.getRepeatedData(), new TypeReference<List<ItemData>>() {
        });
        builder.repeatedData(repeatedData);
      }
      catch (Exception e) {
        throw new BssException("unmarshal repeated data failed", e);
      }
    }
  }

  private static void setDataProperties(Item.ItemBuilder builder, DatasetItemEntity po) {
    if (po.getDataProperties() != null && !po.getDataProperties().isEmpty()) {
      try {
        ItemDataProperties dataProperties = JsonUtil.parseJson(po.getDataProperties(), ItemDataProperties.class);
        builder.dataProperties(dataProperties);
      }
      catch (Exception e) {
        throw new BssException("unmarshal data properties failed", e);
      }
    }
  }
}
