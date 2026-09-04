package com.iwhalecloud.bote.loop.data.application.convertor;

import com.iwhalecloud.bote.loop.client.data.domain.dataset.DatasetItemDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.FieldDataDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.ItemDataDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.ItemErrorDetailDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.ItemErrorGroupDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.ItemErrorTypeDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.ObjectStorageDTO;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.FieldData;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.Item;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.ItemData;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.ItemErrorDetail;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.ItemErrorGroup;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.ItemErrorType;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.ObjectStorage;
import com.iwhalecloud.bote.loop.data.domain.entity.Provider;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据集项目转换器
 * 迁移对应关系: Go语言backend/modules/data/application/convertor/dataset/item.go
 * - 功能: 数据集项目相关的DO和DTO转换
 * - 主要方法:
 * * itemDO2DTO - 数据集项目DO转DTO
 * * itemDataDO2DTO - 项目数据DO转DTO
 * * fieldDataDO2DTO - 字段数据DO转DTO
 * * objectStorageDO2DTO - 对象存储DO转DTO
 * * itemDTO2DO - 数据集项目DTO转DO
 * * itemDataDTO2DO - 项目数据DTO转DO
 * * fieldDataDTO2DO - 字段数据DTO转DO
 * * objectStorageDTO2DO - 对象存储DTO转DO
 * * itemErrorGroupDO2DTO - 项目错误组DO转DTO
 * * itemErrorDetailDO2DTO - 项目错误详情DO转DTO
 * * itemErrorGroupDTO2DO - 项目错误组DTO转DO
 * * itemErrorDetailDTO2DO - 项目错误详情DTO转DO
 * <p>
 * Java实现说明:
 * - 对应Go的item.go文件
 * - 使用静态方法进行转换
 * - 处理嵌套对象转换
 * - 包含时间戳转换逻辑
 * <p>
 * 技术栈迁移:
 * - Go枚举 -> Java枚举
 * - Go switch语句 -> Java switch表达式
 * - Go error返回 -> Java异常处理
 * - Go指针操作 -> Java对象操作
 * - Go时间戳 -> Java时间戳
 */
public final class DatasetItemConvertor {

  private DatasetItemConvertor() {
    // 工具类，禁止实例化
  }

  /**
   * 数据集项目DO转DTO
   * 迁移对应关系: Go语言ItemDO2DTO方法
   *
   * @param item 数据集项目DO
   * @return 数据集项目DTO
   */
  public static DatasetItemDTO itemDO2DTO(Item item) {
    if (item == null) {
      return null;
    }

    List<FieldDataDTO> data = null;
    if (item.getData() != null) {
      data = item.getData().stream()
        .map(DatasetItemConvertor::fieldDataDO2DTO)
        .collect(Collectors.toList());
    }

    List<ItemDataDTO> repeatedData = null;
    if (item.getRepeatedData() != null) {
      repeatedData = item.getRepeatedData().stream()
        .map(DatasetItemConvertor::itemDataDO2DTO)
        .collect(Collectors.toList());
    }

    DatasetItemDTO dto = new DatasetItemDTO();
    dto.setId(item.getId());
    dto.setAppId(item.getAppId());
    dto.setSpaceId(item.getSpaceId());
    dto.setDatasetId(item.getDatasetId());
    dto.setSchemaId(item.getSchemaId());
    dto.setItemId(item.getItemId());
    dto.setItemKey(item.getItemKey());
    dto.setData(data);
    dto.setRepeatedData(repeatedData);
    dto.setCreatedBy(item.getCreatedBy());
    dto.setCreatedAt(item.getCreatedAt() == null ? null : item.getCreatedAt().getTime());
    dto.setUpdatedBy(item.getUpdatedBy());
    dto.setUpdatedAt(item.getUpdatedAt() == null ? null : item.getUpdatedAt().getTime());
    dto.setDataOmitted(false); // Notice: hard code to false

    return dto;
  }

  /**
   * 项目数据DO转DTO
   * 迁移对应关系: Go语言ItemDataDO2DTO方法
   *
   * @param itemData 项目数据DO
   * @return 项目数据DTO
   */
  public static ItemDataDTO itemDataDO2DTO(ItemData itemData) {
    if (itemData == null) {
      return null;
    }

    List<FieldDataDTO> data = null;
    if (itemData.getData() != null) {
      data = itemData.getData().stream()
        .map(DatasetItemConvertor::fieldDataDO2DTO)
        .collect(Collectors.toList());
    }

    ItemDataDTO dto = new ItemDataDTO();
    dto.setId(itemData.getId());
    dto.setData(data);
    return dto;
  }

  /**
   * 字段数据DO转DTO
   * 迁移对应关系: Go语言FieldDataDO2DTO方法
   *
   * @param fieldData 字段数据DO
   * @return 字段数据DTO
   */
  public static FieldDataDTO fieldDataDO2DTO(FieldData fieldData) {
    if (fieldData == null) {
      return null;
    }

    List<ObjectStorageDTO> attachments = null;
    if (fieldData.getAttachments() != null) {
      attachments = fieldData.getAttachments().stream()
        .map(DatasetItemConvertor::objectStorageDO2DTO)
        .collect(Collectors.toList());
    }

    List<FieldDataDTO> parts = null;
    if (fieldData.getParts() != null) {
      parts = fieldData.getParts().stream()
        .map(DatasetItemConvertor::fieldDataDO2DTO)
        .collect(Collectors.toList());
    }

    FieldDataDTO dto = new FieldDataDTO();
    dto.setKey(fieldData.getKey());
    dto.setName(fieldData.getName());
    dto.setContentType(DatasetSchemaConvertor.contentTypeDO2DTO(fieldData.getContentType()));
    dto.setFormat(DatasetSchemaConvertor.fieldDisplayFormatDO2DTO(fieldData.getFormat()));
    dto.setContent(fieldData.getContent());
    dto.setAttachments(attachments);
    dto.setParts(parts);
    return dto;
  }

  /**
   * 对象存储DO转DTO
   * 迁移对应关系: Go语言ObjectStorageDO2DTO方法
   *
   * @param objectStorage 对象存储DO
   * @return 对象存储DTO
   */
  public static ObjectStorageDTO objectStorageDO2DTO(ObjectStorage objectStorage) {
    if (objectStorage == null) {
      return null;
    }

    ObjectStorageDTO dto = new ObjectStorageDTO();
    dto.setProvider(DatasetStorageConvertor.providerDO2DTO(Provider.fromValue(objectStorage.getProvider())));
    dto.setName(objectStorage.getName());
    dto.setUri(objectStorage.getUri());
    dto.setUrl(objectStorage.getUrl());
    dto.setThumbUrl(objectStorage.getThumbUrl());
    return dto;
  }

  /**
   * 数据集项目DTO转DO
   * 迁移对应关系: Go语言ItemDTO2DO方法
   *
   * @param dto 数据集项目DTO
   * @return 数据集项目DO
   */
  public static Item itemDTO2DO(DatasetItemDTO dto) {
    if (dto == null) {
      return null;
    }

    List<FieldData> data = null;
    if (dto.getData() != null) {
      data = dto.getData().stream()
        .map(DatasetItemConvertor::fieldDataDTO2DO)
        .collect(Collectors.toList());
    }

    List<ItemData> repeatedData = null;
    if (dto.getRepeatedData() != null) {
      repeatedData = dto.getRepeatedData().stream()
        .map(DatasetItemConvertor::itemDataDTO2DO)
        .collect(Collectors.toList());
    }

    Item item = new Item();
    item.setId(dto.getId());
    item.setAppId(dto.getAppId());
    item.setSpaceId(dto.getSpaceId());
    item.setDatasetId(dto.getDatasetId());
    item.setSchemaId(dto.getSchemaId());
    item.setItemId(dto.getItemId());
    item.setItemKey(dto.getItemKey());
    item.setData(data);
    item.setRepeatedData(repeatedData);
    item.setCreatedBy(dto.getCreatedBy());
    // 处理createdAt可能为null的情况
    Date now = new Date();
    if (dto.getCreatedAt() != null) {
      item.setCreatedAt(new Date(dto.getCreatedAt()));
    } else {
      item.setCreatedAt(now);
    }
    item.setUpdatedBy(dto.getUpdatedBy());
    // 处理updatedAt可能为null的情况
    if (dto.getUpdatedAt() != null) {
      item.setUpdatedAt(new Date(dto.getUpdatedAt()));
    } else {
      item.setUpdatedAt(now);
    }

    return item;
  }

  /**
   * 项目数据DTO转DO
   * 迁移对应关系: Go语言ItemDataDTO2DO方法
   *
   * @param dto 项目数据DTO
   * @return 项目数据DO
   */
  public static ItemData itemDataDTO2DO(ItemDataDTO dto) {
    if (dto == null) {
      return null;
    }

    List<FieldData> data = null;
    if (dto.getData() != null) {
      data = dto.getData().stream()
        .map(DatasetItemConvertor::fieldDataDTO2DO)
        .collect(Collectors.toList());
    }

    ItemData itemData = new ItemData();
    itemData.setId(dto.getId());
    itemData.setData(data);
    return itemData;
  }

  /**
   * 字段数据DTO转DO
   * 迁移对应关系: Go语言FieldDataDTO2DO方法
   *
   * @param dto 字段数据DTO
   * @return 字段数据DO
   */
  public static FieldData fieldDataDTO2DO(FieldDataDTO dto) {
    if (dto == null) {
      return null;
    }

    List<ObjectStorage> attachments = null;
    if (dto.getAttachments() != null) {
      attachments = dto.getAttachments().stream()
        .map(DatasetItemConvertor::objectStorageDTO2DO)
        .collect(Collectors.toList());
    }

    List<FieldData> parts = null;
    if (dto.getParts() != null) {
      parts = dto.getParts().stream()
        .map(DatasetItemConvertor::fieldDataDTO2DO)
        .collect(Collectors.toList());
    }

    FieldData fieldData = new FieldData();
    fieldData.setKey(dto.getKey());
    fieldData.setName(dto.getName());
    fieldData.setContentType(DatasetSchemaConvertor.contentTypeDTO2DO(dto.getContentType()));
    fieldData.setFormat(DatasetSchemaConvertor.fieldDisplayFormatDTO2DO(dto.getFormat()));
    fieldData.setContent(dto.getContent());
    fieldData.setAttachments(attachments);
    fieldData.setParts(parts);
    return fieldData;
  }

  /**
   * 对象存储DTO转DO
   * 迁移对应关系: Go语言ObjectStorageDTO2DO方法
   *
   * @param dto 对象存储DTO
   * @return 对象存储DO
   */
  public static ObjectStorage objectStorageDTO2DO(ObjectStorageDTO dto) {
    if (dto == null) {
      return null;
    }

    ObjectStorage objectStorage = new ObjectStorage();
    objectStorage.setProvider(DatasetStorageConvertor.storageProviderDTO2DO(dto.getProvider()).getValue());
    objectStorage.setName(dto.getName());
    objectStorage.setUri(dto.getUri());
    objectStorage.setUrl(dto.getUrl());
    objectStorage.setThumbUrl(dto.getThumbUrl());
    return objectStorage;
  }

  /**
   * 项目错误组DO转DTO
   * 迁移对应关系: Go语言ItemErrorGroupDO2DTO方法
   *
   * @param errorGroup 项目错误组DO
   * @return 项目错误组DTO
   */
  public static ItemErrorGroupDTO itemErrorGroupDO2DTO(ItemErrorGroup errorGroup) {
    if (errorGroup == null) {
      return null;
    }

    List<ItemErrorDetailDTO> details = null;
    if (errorGroup.getDetails() != null) {
      details = errorGroup.getDetails().stream()
        .map(DatasetItemConvertor::itemErrorDetailDO2DTO)
        .collect(Collectors.toList());
    }

    ItemErrorGroupDTO dto = new ItemErrorGroupDTO();
    dto.setType(convertItemErrorTypeDO2DTO(errorGroup.getType()));
    dto.setSummary(errorGroup.getSummary());
    dto.setErrorCount(errorGroup.getErrorCount());
    dto.setDetails(details);
    return dto;
  }

  /**
   * 项目错误详情DO转DTO
   * 迁移对应关系: Go语言ItemErrorDetailDO2DTO方法
   *
   * @param errorDetail 项目错误详情DO
   * @return 项目错误详情DTO
   */
  public static ItemErrorDetailDTO itemErrorDetailDO2DTO(ItemErrorDetail errorDetail) {
    if (errorDetail == null) {
      return null;
    }

    ItemErrorDetailDTO dto = new ItemErrorDetailDTO();
    dto.setIndex(errorDetail.getIndex());
    dto.setStartIndex(errorDetail.getStartIndex());
    dto.setEndIndex(errorDetail.getEndIndex());
    dto.setMessage(errorDetail.getMessage());
    return dto;
  }

  /**
   * 项目错误组DTO转DO
   * 迁移对应关系: Go语言ItemErrorGroupDTO2DO方法
   *
   * @param dto 项目错误组DTO
   * @return 项目错误组DO
   */
  public static ItemErrorGroup itemErrorGroupDTO2DO(ItemErrorGroupDTO dto) {
    if (dto == null) {
      return null;
    }

    List<ItemErrorDetail> details = null;
    if (dto.getDetails() != null) {
      details = dto.getDetails().stream()
        .map(DatasetItemConvertor::itemErrorDetailDTO2DO)
        .collect(Collectors.toList());
    }

    ItemErrorGroup errorGroup = new ItemErrorGroup();
    errorGroup.setType(convertItemErrorTypeDTO2DO(dto.getType()));
    errorGroup.setSummary(dto.getSummary());
    errorGroup.setErrorCount(dto.getErrorCount());
    errorGroup.setDetails(details);
    return errorGroup;
  }

  /**
   * 项目错误详情DTO转DO
   * 迁移对应关系: Go语言ItemErrorDetailDTO2DO方法
   *
   * @param dto 项目错误详情DTO
   * @return 项目错误详情DO
   */
  public static ItemErrorDetail itemErrorDetailDTO2DO(ItemErrorDetailDTO dto) {
    if (dto == null) {
      return null;
    }

    ItemErrorDetail errorDetail = new ItemErrorDetail();
    errorDetail.setIndex(dto.getIndex());
    errorDetail.setStartIndex(dto.getStartIndex());
    errorDetail.setEndIndex(dto.getEndIndex());
    errorDetail.setMessage(dto.getMessage());
    return errorDetail;
  }

  /**
   * 项目错误类型DO转DTO
   * 迁移对应关系: Go语言ItemErrorType转换
   *
   * @param type DO层项目错误类型
   * @return DTO层项目错误类型
   */
  private static ItemErrorTypeDTO convertItemErrorTypeDO2DTO(ItemErrorType type) {
    return ItemErrorTypeDTO.fromValue(type.getValue());
  }

  /**
   * 项目错误类型DTO转DO
   * 迁移对应关系: Go语言ItemErrorType转换
   *
   * @param type DTO层项目错误类型
   * @return DO层项目错误类型
   */
  private static ItemErrorType convertItemErrorTypeDTO2DO(ItemErrorTypeDTO type) {
    return ItemErrorType.fromValue(type.getValue());
  }
}
