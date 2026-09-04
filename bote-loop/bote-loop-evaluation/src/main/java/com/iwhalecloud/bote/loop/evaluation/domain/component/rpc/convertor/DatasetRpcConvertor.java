package com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.convertor;

import com.iwhalecloud.bote.loop.client.data.domain.dataset.ContentTypeDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.DatasetDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.DatasetFeaturesDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.DatasetItemDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.DatasetSchemaDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.DatasetSpecDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.DatasetVersionDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.FieldDataDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.FieldDisplayFormatDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.FieldSchemaDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.FieldStatusDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.ItemErrorDetailDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.ItemErrorGroupDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.JSONSchemaDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.MultiModalSpecDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.OrderByDTO;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.MultiModalSpec;
import com.iwhalecloud.bote.loop.evaluation.application.convertor.common.CommonConvertor;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.BaseInfo;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Content;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.DatasetFeatures;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.DatasetSpec;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.DatasetStatus;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluationSet;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluationSetItem;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluationSetSchema;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluationSetVersion;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.FieldData;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.FieldSchema;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ItemErrorDetail;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ItemErrorGroup;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ItemErrorType;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.OrderBy;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Turn;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.UserInfo;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class DatasetRpcConvertor {

  private DatasetRpcConvertor() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

  public static List<FieldSchemaDTO> convert2DatasetFieldSchemas(List<FieldSchema> schemas) {
    if (schemas == null || schemas.isEmpty()) {
      return Collections.emptyList();
    }
    return schemas.stream()
      .map(DatasetRpcConvertor::convert2DatasetFieldSchema)
      .collect(Collectors.toList());
  }

  public static FieldSchemaDTO convert2DatasetFieldSchema(FieldSchema schema) {
    if (schema == null) {
      return null;
    }
    FieldSchemaDTO fieldSchema = new FieldSchemaDTO();

    fieldSchema.setKey(schema.getKey());
    fieldSchema.setName(schema.getName());
    fieldSchema.setDescription(schema.getDescription());

    if (schema.getContentType() != null && !"".equals(schema.getContentType().toString())) {
      ContentTypeDTO contentType = ContentTypeDTO.fromValue(CommonConvertor.convertContentTypeDO2DTO(schema.getContentType()));
      fieldSchema.setContentType(contentType);
    }

    fieldSchema.setDefaultFormat(FieldDisplayFormatDTO.fromString(schema.getDefaultDisplayFormat().getValue()));
    fieldSchema.setStatus(FieldStatusDTO.fromString(schema.getStatus().getDescription()));
    fieldSchema.setMultiModelSpec(convert2DatasetMultiModalSpec(schema.getMultiModelSpec()));

    JSONSchemaDTO jsonSchemaDTO = new JSONSchemaDTO();
    jsonSchemaDTO.setRaw(schema.getTextSchema());
    fieldSchema.setTextSchema(jsonSchemaDTO);
    fieldSchema.setHidden(schema.getHidden());
    fieldSchema.setIsRequired(schema.getIsRequired());
    return fieldSchema;
  }

  public static MultiModalSpecDTO convert2DatasetMultiModalSpec(MultiModalSpec multiModalSpec) {
    if (multiModalSpec == null) {
      return null;
    }
    MultiModalSpecDTO multiModalSpecDTO = new MultiModalSpecDTO();
    multiModalSpecDTO.setMaxFileCount(multiModalSpec.getMaxFileCount());
    multiModalSpecDTO.setMaxFileSize(multiModalSpec.getMaxFileSize());
    multiModalSpecDTO.setSupportedFormats(multiModalSpec.getSupportedFormats());
    return multiModalSpecDTO;
  }

  public static List<OrderByDTO> convert2DatasetOrderBys(List<OrderBy> orderBys) {
    if (orderBys == null || orderBys.isEmpty()) {
      return Collections.emptyList();
    }
    return orderBys.stream()
      .map(DatasetRpcConvertor::convert2DatasetOrderBy)
      .collect(Collectors.toList());
  }

  public static OrderByDTO convert2DatasetOrderBy(OrderBy orderBy) {
    if (orderBy == null) {
      return null;
    }
    OrderByDTO orderByDTO = new OrderByDTO();
    orderByDTO.setField(orderBy.getField());
    orderByDTO.setIsAsc(orderBy.getIsAsc());
    return orderByDTO;
  }

  public static List<FieldDataDTO> convert2DatasetData(List<Turn> turns) {
    if (turns == null || turns.isEmpty()) {
      return Collections.emptyList();
    }
    // 单轮只取第一个元素
    Turn turn = turns.get(0);
    return turn.getFieldDataList().stream()
      .map(DatasetRpcConvertor::convert2DatasetFieldData)
      .collect(Collectors.toList());
  }

  public static FieldDataDTO convert2DatasetFieldData(FieldData fieldData) {
    if (fieldData == null) {
      return null;
    }
    FieldDataDTO datasetFieldData = new FieldDataDTO();
    datasetFieldData.setKey(fieldData.getKey());
    datasetFieldData.setName(fieldData.getName());

    if (fieldData.getContent() != null) {
      ContentTypeDTO contentType = null;
      if (fieldData.getContent().getContentType() != null) {
        contentType = ContentTypeDTO.fromValue(CommonConvertor.convertContentTypeDO2DTO(fieldData.getContent().getContentType()));
      }
      datasetFieldData.setContentType(contentType);
      if (fieldData.getContent().getFormat() != null && fieldData.getContent().getFormat().getDescription() != null) {
        datasetFieldData.setFormat(FieldDisplayFormatDTO.fromString(fieldData.getContent().getFormat().getDescription()));
      }
      // TODO image multi-parts本期不支持，故暂不实现
      datasetFieldData.setContent(fieldData.getContent().getText());
    }

    return datasetFieldData;
  }

  public static DatasetItemDTO convert2DatasetItem(EvaluationSetItem item) {
    if (item == null) {
      return null;
    }
    List<FieldDataDTO> data = convert2DatasetData(item.getTurns());
    DatasetItemDTO datasetItem = new DatasetItemDTO();
    datasetItem.setId(item.getId());
    datasetItem.setAppId(item.getAppId());
    datasetItem.setSpaceId(item.getSpaceId());
    datasetItem.setDatasetId(item.getEvaluationSetId());
    datasetItem.setSchemaId(item.getSchemaId());
    datasetItem.setItemId(item.getItemId());
    datasetItem.setItemKey(item.getItemKey());
    datasetItem.setData(data);
    return datasetItem;
  }

  public static List<DatasetItemDTO> convert2DatasetItems(List<EvaluationSetItem> items) {
    if (items == null || items.isEmpty()) {
      return Collections.emptyList();
    }
    return items.stream()
      .map(DatasetRpcConvertor::convert2DatasetItem)
      .collect(Collectors.toList());
  }

  public static DatasetSpec convert2EvaluationSetSpec(DatasetSpecDTO spec) {
    if (spec == null) {
      return null;
    }
    DatasetSpec evaluationSetSpec = new DatasetSpec();
    evaluationSetSpec.setMaxFieldCount(spec.getMaxFieldCount());
    evaluationSetSpec.setMaxItemCount(spec.getMaxItemCount());
    evaluationSetSpec.setMaxItemSize(spec.getMaxItemSize());
    return evaluationSetSpec;
  }

  public static DatasetFeatures convert2DatasetFeatures(DatasetFeaturesDTO features) {
    if (features == null) {
      return null;
    }
    DatasetFeatures evaluationSetFeatures = new DatasetFeatures();
    evaluationSetFeatures.setEditSchema(features.getEditSchema());
    evaluationSetFeatures.setRepeatedData(features.getRepeatedData());
    evaluationSetFeatures.setMultiModal(features.getMultiModal());
    return evaluationSetFeatures;
  }

  public static MultiModalSpec convert2EvaluationSetMultiModalSpec(MultiModalSpecDTO multiModalSpec) {
    if (multiModalSpec == null) {
      return null;
    }
    MultiModalSpec evaluationSetMultiModalSpec = new MultiModalSpec();
    evaluationSetMultiModalSpec.setMaxFileCount(multiModalSpec.getMaxFileCount());
    evaluationSetMultiModalSpec.setMaxFileSize(multiModalSpec.getMaxFileSize());
    evaluationSetMultiModalSpec.setSupportedFormats(multiModalSpec.getSupportedFormats());
    return evaluationSetMultiModalSpec;
  }

  public static List<FieldSchema> convert2EvaluationSetFieldSchemas(List<FieldSchemaDTO> schemas) {
    if (schemas == null || schemas.isEmpty()) {
      return Collections.emptyList();
    }
    return schemas.stream()
      .map(DatasetRpcConvertor::convert2EvaluationSetFieldSchema)
      .collect(Collectors.toList());
  }

  public static FieldSchema convert2EvaluationSetFieldSchema(FieldSchemaDTO schema) {
    if (schema == null) {
      return null;
    }
    FieldSchema fieldSchema = new FieldSchema();
    fieldSchema.setKey(schema.getKey());
    fieldSchema.setName(schema.getName());
    fieldSchema.setDescription(schema.getDescription());
    if (schema.getContentType() != null) {
      fieldSchema.setContentType(CommonConvertor.convertContentTypeDTO2DO(schema.getContentType().getName()));
    }
    if (schema.getDefaultFormat() != null) {
      fieldSchema.setDefaultDisplayFormat(com.iwhalecloud.bote.loop.data.domain.dataset.entity.FieldDisplayFormat.fromValue(schema.getDefaultFormat().getDescription()));
    }
    if (schema.getStatus() != null) {
      fieldSchema.setStatus(com.iwhalecloud.bote.loop.data.domain.dataset.entity.FieldStatus.fromString(schema.getStatus().getDescription()));
    }
    fieldSchema.setMultiModelSpec(convert2EvaluationSetMultiModalSpec(schema.getMultiModelSpec()));
    if (schema.getTextSchema() != null) {
      fieldSchema.setTextSchema(schema.getTextSchema().getRaw());
    }
    fieldSchema.setHidden(schema.getHidden());
    fieldSchema.setIsRequired(schema.getIsRequired());
    return fieldSchema;
  }

  public static EvaluationSetSchema convert2EvaluationSetSchema(DatasetSchemaDTO schema) {
    if (schema == null) {
      return null;
    }
    EvaluationSetSchema datasetSchema = new EvaluationSetSchema();
    datasetSchema.setId(schema.getId());
    datasetSchema.setAppId(schema.getAppId());
    datasetSchema.setSpaceId(schema.getSpaceId());
    datasetSchema.setEvaluationSetId(schema.getDatasetId());
    datasetSchema.setFieldSchemas(convert2EvaluationSetFieldSchemas(schema.getFields()));

    BaseInfo baseInfo = new BaseInfo();
    baseInfo.setCreatedAt(schema.getCreatedAt());
    baseInfo.setUpdatedAt(schema.getUpdatedAt());

    UserInfo createdBy = new UserInfo();
    createdBy.setUserId(schema.getCreatedBy());
    UserInfo updatedBy = new UserInfo();
    updatedBy.setUserId(schema.getUpdatedBy());

    baseInfo.setCreatedBy(createdBy);
    baseInfo.setUpdatedBy(updatedBy);
    datasetSchema.setBaseInfo(baseInfo);

    return datasetSchema;
  }

  public static EvaluationSetVersion convert2EvaluationSetDraftVersion(DatasetDTO dataset) {
    if (dataset == null) {
      return null;
    }
    EvaluationSetVersion evaluationSetVersion = new EvaluationSetVersion();
    evaluationSetVersion.setId(dataset.getId());
    evaluationSetVersion.setAppId(dataset.getAppId());
    evaluationSetVersion.setSpaceId(dataset.getSpaceId());
    evaluationSetVersion.setEvaluationSetId(dataset.getId());
    evaluationSetVersion.setDescription(dataset.getDescription());
    evaluationSetVersion.setEvaluationSetSchema(convert2EvaluationSetSchema(dataset.getSchema()));
    evaluationSetVersion.setItemCount(dataset.getItemCount());

    BaseInfo baseInfo = new BaseInfo();
    baseInfo.setCreatedAt(dataset.getCreatedAt());
    UserInfo createdBy = new UserInfo();
    createdBy.setUserId(dataset.getCreatedBy());
    baseInfo.setCreatedBy(createdBy);
    evaluationSetVersion.setBaseInfo(baseInfo);

    return evaluationSetVersion;
  }

  public static List<EvaluationSet> convert2EvaluationSets(List<DatasetDTO> datasets) {
    if (datasets == null || datasets.isEmpty()) {
      return Collections.emptyList();
    }
    return datasets.stream()
      .map(DatasetRpcConvertor::convert2EvaluationSet)
      .collect(Collectors.toList());
  }

  public static EvaluationSet convert2EvaluationSet(DatasetDTO dataset) {
    if (dataset == null) {
      return null;
    }
    EvaluationSet evaluationSet = new EvaluationSet();
    evaluationSet.setId(dataset.getId());
    evaluationSet.setAppId(dataset.getAppId());
    evaluationSet.setSpaceId(dataset.getSpaceId());
    evaluationSet.setName(dataset.getName());
    evaluationSet.setDescription(dataset.getDescription());
    evaluationSet.setStatus(DatasetStatus.fromValue(dataset.getStatus().getValue()));
    evaluationSet.setSpec(convert2EvaluationSetSpec(dataset.getSpec()));
    evaluationSet.setFeatures(convert2DatasetFeatures(dataset.getFeatures()));
    evaluationSet.setItemCount(dataset.getItemCount());
    evaluationSet.setChangeUncommitted(dataset.getChangeUncommitted());
    evaluationSet.setEvaluationSetVersion(convert2EvaluationSetDraftVersion(dataset));
    evaluationSet.setLatestVersion(dataset.getLatestVersion());
    evaluationSet.setNextVersionNum(dataset.getNextVersionNum());
    evaluationSet.setCatalogItemId(dataset.getCatalogItemId());

    BaseInfo baseInfo = new BaseInfo();
    baseInfo.setCreatedAt(dataset.getCreatedAt());
    baseInfo.setUpdatedAt(dataset.getUpdatedAt());
    UserInfo createdBy = new UserInfo();
    createdBy.setUserId(dataset.getCreatedBy());
    UserInfo updatedBy = new UserInfo();
    updatedBy.setUserId(dataset.getUpdatedBy());

    baseInfo.setCreatedBy(createdBy);
    baseInfo.setUpdatedBy(updatedBy);
    evaluationSet.setBaseInfo(baseInfo);

    return evaluationSet;
  }

  public static List<EvaluationSetVersion> convert2EvaluationSetVersions(List<DatasetVersionDTO> versions) {
    if (versions == null || versions.isEmpty()) {
      return Collections.emptyList();
    }
    return versions.stream()
      .map(version -> convert2EvaluationSetVersion(version, new DatasetDTO()))
      .collect(Collectors.toList());
  }

  public static EvaluationSetVersion convert2EvaluationSetVersion(DatasetVersionDTO version) {
    return convert2EvaluationSetVersion(version, new DatasetDTO());
  }
  public static EvaluationSetVersion convert2EvaluationSetVersion(DatasetVersionDTO version, DatasetDTO dataset) {
    if (version == null) {
      return null;
    }
    EvaluationSetVersion evaluationSetVersion = new EvaluationSetVersion();
    evaluationSetVersion.setId(version.getId());
    evaluationSetVersion.setAppId(version.getAppId());
    evaluationSetVersion.setSpaceId(version.getSpaceId());
    evaluationSetVersion.setEvaluationSetId(version.getDatasetId());
    evaluationSetVersion.setVersion(version.getVersion());
    evaluationSetVersion.setVersionNum(version.getVersionNum());
    evaluationSetVersion.setDescription(version.getDescription());
    evaluationSetVersion.setItemCount(version.getItemCount());

    BaseInfo baseInfo = new BaseInfo();
    baseInfo.setCreatedAt(version.getCreatedAt());
    UserInfo createdBy = new UserInfo();
    createdBy.setUserId(version.getCreatedBy());

    baseInfo.setCreatedBy(createdBy);
    evaluationSetVersion.setBaseInfo(baseInfo);

    if (dataset != null) {
      evaluationSetVersion.setEvaluationSetSchema(convert2EvaluationSetSchema(dataset.getSchema()));
    }

    return evaluationSetVersion;
  }

  public static FieldData convert2EvaluationSetFieldData(FieldDataDTO fieldData) {
    if (fieldData == null) {
      return null;
    }
    FieldData evalSetFieldData = new FieldData();
    evalSetFieldData.setKey(fieldData.getKey());
    evalSetFieldData.setName(fieldData.getName());

    Content content = new Content();
    content.setContentType(CommonConvertor.convertContentTypeDTO2DO(fieldData.getContentType().getName()));
    content.setFormat(CommonConvertor.convertFieldDisplayFormatDTO2DO((long) fieldData.getFormat().getValue()));
    // TODO image multi-parts本期不支持，故暂不实现
    content.setText(fieldData.getContent());
    evalSetFieldData.setContent(content);

    return evalSetFieldData;
  }

  public static List<Turn> convert2EvaluationSetTurn(List<FieldDataDTO> data) {
    if (data == null || data.isEmpty()) {
      return Collections.emptyList();
    }
    Turn turn = new Turn();
    turn.setId(0L);
    turn.setFieldDataList(data.stream()
      .map(DatasetRpcConvertor::convert2EvaluationSetFieldData)
      .collect(Collectors.toList()));
    return List.of(turn);
  }

  public static EvaluationSetItem convert2EvaluationSetItem(DatasetItemDTO item) {
    if (item == null) {
      return null;
    }
    EvaluationSetItem datasetItem = new EvaluationSetItem();
    datasetItem.setId(item.getId());
    datasetItem.setAppId(item.getAppId());
    datasetItem.setSpaceId(item.getSpaceId());
    datasetItem.setEvaluationSetId(item.getDatasetId());
    datasetItem.setSchemaId(item.getSchemaId());
    datasetItem.setItemId(item.getItemId());
    datasetItem.setItemKey(item.getItemKey());
    datasetItem.setTurns(convert2EvaluationSetTurn(item.getData()));

    BaseInfo baseInfo = new BaseInfo();
    baseInfo.setCreatedAt(item.getCreatedAt());
    baseInfo.setUpdatedAt(item.getUpdatedAt());
    UserInfo createdBy = new UserInfo();
    createdBy.setUserId(item.getCreatedBy());
    UserInfo updatedBy = new UserInfo();
    updatedBy.setUserId(item.getUpdatedBy());

    baseInfo.setCreatedBy(createdBy);
    baseInfo.setUpdatedBy(updatedBy);
    datasetItem.setBaseInfo(baseInfo);

    return datasetItem;
  }

  public static List<EvaluationSetItem> convert2EvaluationSetItems(List<DatasetItemDTO> items) {
    if (items == null || items.isEmpty()) {
      return Collections.emptyList();
    }
    return items.stream()
      .map(DatasetRpcConvertor::convert2EvaluationSetItem)
      .collect(Collectors.toList());
  }

  public static List<ItemErrorGroup> convert2EvaluationSetErrorGroups(List<ItemErrorGroupDTO> errors) {
    if (errors == null || errors.isEmpty()) {
      return Collections.emptyList();
    }
    return errors.stream()
      .map(DatasetRpcConvertor::convert2EvaluationSetErrorGroup)
      .collect(Collectors.toList());
  }

  public static ItemErrorGroup convert2EvaluationSetErrorGroup(ItemErrorGroupDTO errorGroup) {
    if (errorGroup == null) {
      return null;
    }
    ItemErrorGroup res = new ItemErrorGroup();
    res.setType(ItemErrorType.fromValue(errorGroup.getType().getValue()));
    res.setSummary(errorGroup.getSummary());
    res.setErrorCount(errorGroup.getErrorCount());
    res.setDetails(convert2EvaluationSetErrorDetails(errorGroup.getDetails()));
    return res;
  }

  public static List<ItemErrorDetail> convert2EvaluationSetErrorDetails(List<ItemErrorDetailDTO> errorDetails) {
    if (errorDetails == null || errorDetails.isEmpty()) {
      return Collections.emptyList();
    }
    return errorDetails.stream()
      .map(DatasetRpcConvertor::convert2EvaluationSetErrorDetail)
      .collect(Collectors.toList());
  }

  public static ItemErrorDetail convert2EvaluationSetErrorDetail(ItemErrorDetailDTO errorDetail) {
    if (errorDetail == null) {
      return null;
    }
    ItemErrorDetail res = new ItemErrorDetail();
    res.setMessage(errorDetail.getMessage());
    res.setIndex(errorDetail.getIndex());
    res.setStartIndex(errorDetail.getStartIndex());
    res.setEndIndex(errorDetail.getEndIndex());
    return res;
  }
}
