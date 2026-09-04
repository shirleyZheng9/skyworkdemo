package com.iwhalecloud.bote.loop.data.application;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.loop.client.data.dataset.DatasetItemService;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.BatchCreateDatasetItemsRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.BatchCreateDatasetItemsResponse;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.BatchDeleteDatasetItemsRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.BatchDeleteDatasetItemsResponse;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.BatchGetDatasetItemsByVersionRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.BatchGetDatasetItemsRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.ClearDatasetItemRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.ClearDatasetItemResponse;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.DeleteDatasetItemRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.DeleteDatasetItemResponse;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.GetDatasetItemRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.GetDatasetItemResponse;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.ListDatasetItemsByVersionRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.ListDatasetItemsRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.UpdateDatasetItemRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.UpdateDatasetItemResponse;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.DatasetItemDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.ItemErrorGroupDTO;
import com.iwhalecloud.bote.loop.data.application.convertor.DatasetItemConvertor;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetOpType;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetSchema;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetVersion;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.Item;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.ItemErrorGroup;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.ItemSnapshot;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.SnapshotStatus;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.IDatasetAPI;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.dto.ListItemSnapshotsParams;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.dto.ListItemsParams;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.DatasetDomainService;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.dto.DatasetWithSchema;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.dto.IndexedItem;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.dto.MAddItemOpt;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.dto.UpdateDatasetParam;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.util.ItemUtils;
import com.iwhalecloud.bote.loop.data.pkg.pagination.Paginator;
import com.iwhalecloud.bote.loop.data.pkg.pagination.PaginatorFactory;
import com.iwhalecloud.bote.loop.infra.session.SessionContext;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

/**
 * 数据集项目管理服务实现类
 * - 功能: 数据集项目管理相关的应用层服务
 * - 主要方法:
 * * batchCreateDatasetItems - 批量新增数据
 * * updateDatasetItem - 更新数据
 * * deleteDatasetItem - 删除数据
 * * batchDeleteDatasetItems - 批量删除数据
 * * listDatasetItems - 分页查询当前数据
 * * listDatasetItemsByVersion - 分页查询指定版本的数据
 * * getDatasetItem - 获取指定数据
 * * batchGetDatasetItems - 批量获取数据
 * * batchGetDatasetItemsByVersion - 批量获取指定版本的数据
 * * clearDatasetItem - 清空数据
 */
@Service
@RequiredArgsConstructor
public class DatasetItemServiceImpl implements DatasetItemService {
  private static final Logger logger = LoggerFactory.getLogger(DatasetItemServiceImpl.class);

  private final IDatasetAPI datasetAPI;
  private final DatasetDomainService datasetDomainService;

  @Override
  public BatchCreateDatasetItemsResponse batchCreateDatasetItems(BatchCreateDatasetItemsRequest request) {
    // 准备数据 - 对应Go代码第35-42行
    BatchCreateDatasetItemsReqContext rc = prepare(request);
    if (rc.dataset() != null) {
      rc.dataset().getDataset().setUpdatedBy(SessionContext.getCurrentUserId());
    }
    if (rc.goodItems().isEmpty()) {
      return buildResp(rc, null);
    }
    MAddItemOpt mAddItemOpt = new MAddItemOpt();
    mAddItemOpt.setPartialAdd(request.getAllowPartialAdd());

    // 批量创建项目
    List<IndexedItem> added = datasetDomainService.batchCreateItems(
      rc.dataset(),
      rc.goodItems(),
      mAddItemOpt
    );

    updateDataset(rc.dataset(), DatasetOpType.WRITE_ITEM);


    return buildResp(rc, added);
  }

  private void updateDataset(DatasetWithSchema datasetWithSchema, DatasetOpType opType) {
    UpdateDatasetParam param = new UpdateDatasetParam();
    param.setSpaceId(datasetWithSchema.getDataset().getSpaceId());
    param.setDatasetId(datasetWithSchema.getDataset().getId());
    param.setLastOperation(opType);
    datasetDomainService.updateDataset(param);
  }

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public UpdateDatasetItemResponse updateDatasetItem(UpdateDatasetItemRequest request) {
    // 获取数据集信息 - 对应Go代码第59-65行
    DatasetWithSchema datasetWithSchema = datasetDomainService.getDataset(
      request.getWorkspaceId(),
      request.getDatasetId()
    );
    if (datasetWithSchema == null) {
      throw new BssException("dataset=" + request.getDatasetId() + " is not found");
    }
    datasetWithSchema.getDataset().setUpdatedBy(SessionContext.getCurrentUserId());

    // 获取项目信息 - 对应Go代码第66-70行
    Item item = datasetDomainService.getItem(
      request.getWorkspaceId(),
      request.getDatasetId(),
      request.getItemId()
    );
    Assert.notNull(item, "item=" + request.getItemId() + " is not found");
    Long oldId = item.getId();
    boolean inPlace = item.getAddVN().equals(datasetWithSchema.getDataset().getNextVersionNum());
    buildItem(request, datasetWithSchema, item, inPlace);
    ItemUtils.validateItem(datasetWithSchema, item);
    if (inPlace) {
      logger.info("update item in place, id={}, item_id={}", datasetWithSchema.getDataset().getId(), item.getId());
      datasetDomainService.updateItem(datasetWithSchema, item);
    }
    else {
      logger.info("archive and create item, id={}, item_id={}", datasetWithSchema.getDataset().getId(), item.getId());
      datasetDomainService.archiveAndCreateItem(datasetWithSchema, oldId, item);
    }

    updateDataset(datasetWithSchema, DatasetOpType.WRITE_ITEM);

    return new UpdateDatasetItemResponse();
  }

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public DeleteDatasetItemResponse deleteDatasetItem(DeleteDatasetItemRequest request) {
    // 鉴权 - 对应Go代码第95-99行
    // TODO: 实现鉴权逻辑

    // 获取数据集信息 - 对应Go代码第100-105行
    DatasetWithSchema datasetWithSchema = datasetDomainService.getDataset(
      request.getWorkspaceId(),
      request.getDatasetId()
    );
    if (datasetWithSchema == null) {
      throw new BssException("dataset=" + request.getDatasetId() + " is not found");
    }
    datasetWithSchema.getDataset().setUpdatedBy(SessionContext.getCurrentUserId());

    // 获取项目信息 - 对应Go代码第106-110行
    Item item = datasetDomainService.getItem(
      request.getWorkspaceId(),
      request.getDatasetId(),
      request.getItemId()
    );
    if (item == null) {
      throw new BssException("item=" + request.getItemId() + " is not found");
    }

    // 批量删除项目 - 对应Go代码第111-113行
    datasetDomainService.batchDeleteItems(datasetWithSchema, item);
    updateDataset(datasetWithSchema, DatasetOpType.WRITE_ITEM);

    logger.info("delete dataset item success, space_id={}, dataset_id={}, item_id={}",
      request.getWorkspaceId(), request.getDatasetId(), request.getItemId());

    return new DeleteDatasetItemResponse();
  }

  @Override
  public BatchDeleteDatasetItemsResponse batchDeleteDatasetItems(BatchDeleteDatasetItemsRequest request) {
    DatasetWithSchema datasetWithSchema = datasetDomainService.getDataset(
      request.getWorkspaceId(),
      request.getDatasetId()
    );
    Assert.notNull(datasetWithSchema, "dataset=" + request.getDatasetId() + " is not found");
    datasetWithSchema.getDataset().setUpdatedBy(SessionContext.getCurrentUserId());
    PageInfo<Item> items = datasetDomainService.batchGetItems(
      request.getWorkspaceId(),
      request.getDatasetId(),
      request.getItemIds()
    );
    datasetDomainService.batchDeleteItems(datasetWithSchema, items.getList().toArray(new Item[0]));
    updateDataset(datasetWithSchema, DatasetOpType.WRITE_ITEM);
    return new BatchDeleteDatasetItemsResponse();
  }

  @Override
  public PageInfo<DatasetItemDTO> listDatasetItems(ListDatasetItemsRequest request) {
    // 鉴权 - 对应Go代码第141-145行
    // TODO: 实现鉴权逻辑

    // 获取数据集信息 - 对应Go代码第146-150行
    DatasetWithSchema datasetWithSchema = datasetDomainService.getDataset(
      request.getWorkspaceId(),
      request.getDatasetId()
    );
    if (datasetWithSchema == null) {
      throw new BssException("dataset=" + request.getDatasetId() + " is not found");
    }
    // 构建查询参数 - 对应Go代码第164-170行
    ListItemsParams query = new ListItemsParams();
    query.setSpaceId(request.getWorkspaceId());
    query.setDatasetId(request.getDatasetId());
    Paginator paginator = PaginatorFactory.newPaginator(request.getOrderBys(), request.getPageSize(), request.getPageNumber());
    query.setPaginator(paginator);
    // 查询项目列表 - 对应Go代码第171-175行
    PageInfo<Item> items = datasetAPI.listItems(query);

    // 加载项目数据 - 对应Go代码第176-179行
    datasetDomainService.loadItemData(items.getList().toArray(new Item[0]));

    // 清理输出项目 - 对应Go代码第181行
    ItemUtils.sanitizeOutputItem(datasetWithSchema.getSchema(), items.getList());

    return items.convert(DatasetItemConvertor::itemDO2DTO);
  }

  @Override
  public PageInfo<DatasetItemDTO> listDatasetItemsByVersion(ListDatasetItemsByVersionRequest request) {
    // 鉴权 - 对应Go代码第194-198行
    // TODO: 实现鉴权逻辑

    // 获取版本信息 - 对应Go代码第199-202行
    DatasetVersion version = datasetAPI.getVersion(request.getWorkspaceId(), request.getVersionId());
    if (version == null) {
      throw new BssException("version=" + request.getVersionId() + " is not found");
    }

    // 查询项目列表 - 对应Go代码第204-208行
    PageInfo<Item> items = listItemsByVersion(request, version);

    // 加载项目数据 - 对应Go代码第209-212行
    datasetDomainService.loadItemData(items.getList().toArray(new Item[0]));

    // 获取模式并清理输出 - 对应Go代码第214-220行
    DatasetSchema schema = datasetAPI.getSchema(request.getWorkspaceId(), version.getSchemaId());
    ItemUtils.sanitizeOutputItem(schema, items.getList());

    return items.convert(DatasetItemConvertor::itemDO2DTO);
  }

  @Override
  public GetDatasetItemResponse getDatasetItem(GetDatasetItemRequest request) {
    // 鉴权 - 对应Go代码第235-239行
    // TODO: 实现鉴权逻辑

    // 获取项目 - 对应Go代码第240-244行
    Item item = datasetDomainService.getItem(
      request.getWorkspaceId(),
      request.getDatasetId(),
      request.getItemId()
    );
    if (item == null) {
      throw new BssException("item=" + request.getItemId() + " is not found");
    }

    // 加载项目数据 - 对应Go代码第246-249行
    datasetDomainService.loadItemData(item);

    // 转换为DTO - 对应Go代码第250行
    DatasetItemDTO itemDTO = DatasetItemConvertor.itemDO2DTO(item);

    // 构建响应
    GetDatasetItemResponse response = new GetDatasetItemResponse();
    response.setItem(itemDTO);

    return response;
  }

  @Override
  public PageInfo<DatasetItemDTO> batchGetDatasetItems(BatchGetDatasetItemsRequest request) {
    // 鉴权 - 对应Go代码第255-259行
    // TODO: 实现鉴权逻辑

    // 获取数据集信息 - 对应Go代码第260-267行
    DatasetWithSchema datasetWithSchema = datasetDomainService.getDataset(
      request.getWorkspaceId(),
      request.getDatasetId()
    );
    if (datasetWithSchema == null) {
      throw new BssException("dataset=" + request.getDatasetId() + " of space=" + request.getWorkspaceId() + " is not found");
    }

    if (CollectionUtils.isEmpty(request.getItemIds())) {
      return new PageInfo<>(Collections.emptyList());
    }

    // 构建查询参数 - 对应Go代码第269-273行
    ListItemsParams query = new ListItemsParams();
    query.setSpaceId(request.getWorkspaceId());
    query.setDatasetId(request.getDatasetId());
    query.setItemIds(request.getItemIds());

    // 查询项目列表 - 对应Go代码第274-278行
    PageInfo<Item> items = datasetAPI.listItems(query);
    // 加载项目数据 - 对应Go代码第280-283行
    datasetDomainService.loadItemData(items.getList().toArray(new Item[0]));
    // 清理输出项目 - 对应Go代码第284行
    ItemUtils.sanitizeOutputItem(datasetWithSchema.getSchema(), items.getList());
    return items.convert(DatasetItemConvertor::itemDO2DTO);
  }

  @Override
  public PageInfo<DatasetItemDTO> batchGetDatasetItemsByVersion(BatchGetDatasetItemsByVersionRequest request) {
    // 鉴权 - 对应Go代码第290-294行
    // TODO: 实现鉴权逻辑

    // 获取版本信息 - 对应Go代码第295-301行
    DatasetVersion version = datasetAPI.getVersion(request.getWorkspaceId(), request.getVersionId());
    if (version == null) {
      throw new BssException("version=" + request.getVersionId() + " of space=" + request.getWorkspaceId() + " is not found");
    }

    if (CollectionUtils.isEmpty(request.getItemIds())) {
      return new PageInfo<>(Collections.emptyList());
    }

    // 构建查询参数 - 对应Go代码第303-307行
    ListItemsParams query = new ListItemsParams();
    query.setSpaceId(version.getSpaceId());
    query.setDatasetId(version.getDatasetId());
    query.setDelVnGt(version.getVersionNum());
    query.setAddVnLte(version.getVersionNum());
    query.setItemIds(request.getItemIds());

    // 查询项目列表 - 对应Go代码第308-312行
    PageInfo<Item> items = datasetAPI.listItems(query);

    // 加载项目数据 - 对应Go代码第313-316行
    datasetDomainService.loadItemData(items.getList().toArray(new Item[0]));

    // 获取模式并清理输出 - 对应Go代码第317-321行
    DatasetSchema schema = datasetAPI.getSchema(version.getSpaceId(), version.getSchemaId());
    ItemUtils.sanitizeOutputItem(schema, items.getList());

    return items.convert(DatasetItemConvertor::itemDO2DTO);
  }

  @Override
  public ClearDatasetItemResponse clearDatasetItem(ClearDatasetItemRequest request) {
    // 鉴权 - 对应Go代码第327-331行
    // TODO: 实现鉴权逻辑

    // 获取数据集信息 - 对应Go代码第332-336行
    DatasetWithSchema datasetWithSchema = datasetDomainService.getDataset(
      request.getWorkspaceId(),
      request.getDatasetId()
    );
    if (datasetWithSchema == null) {
      throw new BssException("dataset=" + request.getDatasetId() + " is not found");
    }
    datasetWithSchema.getDataset().setUpdatedBy(SessionContext.getCurrentUserId());

    // 清空数据集 - 对应Go代码第337-341行
    datasetDomainService.clearDataset(datasetWithSchema);

    return new ClearDatasetItemResponse();
  }

  /**
   * 准备批量创建项目请求上下文
   * 迁移对应关系: Go语言prepare方法
   */
  private BatchCreateDatasetItemsReqContext prepare(BatchCreateDatasetItemsRequest request) {
    validateItemKeys(request);
    DatasetWithSchema datasetWithSchema = getAndValidateDataset(request);
    ItemUtils.ValidationResult validationResult = validateAndSanitizeItems(request, datasetWithSchema);
    validateItemCapacity(request, datasetWithSchema, validationResult.good());

    return new BatchCreateDatasetItemsReqContext(
      datasetWithSchema,
      validationResult.good(),
      validationResult.bad()
    );
  }

  private void validateItemKeys(BatchCreateDatasetItemsRequest request) {
    List<String> keys = request.getItems().stream()
      .filter(item -> item.getItemKey() != null && !item.getItemKey().isEmpty())
      .map(DatasetItemDTO::getItemKey)
      .collect(Collectors.toList());

    List<String> duplicates = findDuplicates(keys);
    if (!duplicates.isEmpty()) {
      throw new BssException("duplicate item keys found: " + String.join(", ", duplicates));
    }
  }

  private DatasetWithSchema getAndValidateDataset(BatchCreateDatasetItemsRequest request) {
    DatasetWithSchema datasetWithSchema = datasetDomainService.getDataset(
      request.getWorkspaceId(),
      request.getDatasetId()
    );
    if (datasetWithSchema == null) {
      throw new BssException("dataset=" + request.getDatasetId() + " is not found");
    }

    if (!datasetWithSchema.getDataset().canWriteItem()) {
      throw new BssException("dataset_status=" + datasetWithSchema.getDataset().getStatus());
    }

    return datasetWithSchema;
  }

  private ItemUtils.ValidationResult validateAndSanitizeItems(BatchCreateDatasetItemsRequest request,
                                                              DatasetWithSchema datasetWithSchema) {
    List<Item> items = convertItemsToDO(request);
    ItemUtils.sanitizeInputItem(datasetWithSchema, items.toArray(new Item[0]));

    return ItemUtils.validateItems(datasetWithSchema, items);
  }

  private List<Item> convertItemsToDO(BatchCreateDatasetItemsRequest request) {
    return request.getItems().stream()
      .map(DatasetItemConvertor::itemDTO2DO)
      .collect(Collectors.toList());
  }

  private void validateItemCapacity(BatchCreateDatasetItemsRequest request, DatasetWithSchema datasetWithSchema,
                                    List<IndexedItem> goodItems) {
    Long total = getCurrentItemCount(request.getDatasetId());
    long remaining = datasetWithSchema.getDataset().getSpec().getMaxItemCount() - total;
    long diff = remaining - goodItems.size();

    if (diff < 0 && !request.getAllowPartialAdd()) {
      throw new BssException("capacity=" + datasetWithSchema.getDataset().getSpec().getMaxItemCount() +
        ", current=" + total + ", to_add=" + goodItems.size());
    }
  }

  private Long getCurrentItemCount(Long datasetId) {
    Long total = datasetAPI.getItemCount(datasetId);
    return total != null ? total : 0L;
  }


  /**
   * 构建响应
   * 迁移对应关系: Go语言buildResp方法
   */
  private BatchCreateDatasetItemsResponse buildResp(BatchCreateDatasetItemsReqContext rc, List<IndexedItem> added) {
    List<ItemErrorGroup> badItems = rc.badItems();
    // 转换为DTO
    List<ItemErrorGroupDTO> errorDTOs = badItems.stream()
      .map(DatasetItemConvertor::itemErrorGroupDO2DTO)
      .collect(Collectors.toList());

    Map<Long, Long> addedItems = new HashMap<>();
    if (added != null) {
      for (IndexedItem item : added) {
        addedItems.put((long) item.getIndex(), item.getItem().getItemId());
      }
    }

    BatchCreateDatasetItemsResponse response = new BatchCreateDatasetItemsResponse();
    response.setAddedItems(addedItems);
    response.setErrors(errorDTOs);

    return response;
  }

  /**
   * 构建项目数据
   * 迁移对应关系: Go语言buildItem方法
   */
  private void buildItem(UpdateDatasetItemRequest request, DatasetWithSchema datasetWithSchema, Item item, boolean inPlace) {
    // 构建补丁数据 - 对应Go代码第420-424行
    Item patch = new Item();
    patch.setData(request.getData().stream()
      .map(DatasetItemConvertor::fieldDataDTO2DO)
      .collect(Collectors.toList()));
    patch.setRepeatedData(request.getRepeatedData() == null ? null : request.getRepeatedData().stream()
      .map(DatasetItemConvertor::itemDataDTO2DO)
      .collect(Collectors.toList()));

    ItemUtils.sanitizeInputItem(datasetWithSchema, patch);
    String userId = SessionContext.getCurrentUserId();

    // 更新项目属性 - 对应Go代码第426-432行
    item.setUpdatedBy(userId);
    item.setData(patch.getData());
    item.setRepeatedData(patch.getRepeatedData());
    item.setUpdatedAt(new Date());
    item.buildProperties();

    if (!inPlace) {
      item.setAddVN(datasetWithSchema.getDataset().getNextVersionNum());
      item.setDelVN(Long.MAX_VALUE); // consts.MaxVersionNum
    }
  }

  /**
   * 根据版本查询项目列表
   * 迁移对应关系: Go语言listItemsByVersion方法
   */
  private PageInfo<Item> listItemsByVersion(ListDatasetItemsByVersionRequest request, DatasetVersion version) {
    if (version.getSnapshotStatus() == SnapshotStatus.COMPLETED) {
      // 从快照查询项目 - 对应Go代码第300-305行
      return listSnapshotsByVersion(request);
    }

    // 从项目表查询 - 对应Go代码第307-315行
    ListItemsParams query = new ListItemsParams();
    query.setSpaceId(version.getSpaceId());
    query.setDatasetId(version.getDatasetId());
    query.setDelVnGt(version.getVersionNum());
    query.setAddVnLte(version.getVersionNum());
    Paginator paginator = PaginatorFactory.newPaginator(request.getOrderBys(), request.getPageSize(), request.getPageNumber());
    query.setPaginator(paginator);
    return datasetAPI.listItems(query);
  }

  /**
   * 从快照查询项目列表
   * 迁移对应关系: Go语言listSnapshotsByVersion方法
   */
  private PageInfo<Item> listSnapshotsByVersion(ListDatasetItemsByVersionRequest request) {
    Paginator paginator = PaginatorFactory.newPaginator(request.getOrderBys(), request.getPageSize(), request.getPageNumber());
    if ("updated_at".equals(paginator.getTimeColumn())) {
      paginator.setTimeColumn("item_updated_at");
    }

    // 查询快照 - 对应Go代码第333-340行
    ListItemSnapshotsParams params = new ListItemSnapshotsParams();
    params.setSpaceId(request.getWorkspaceId());
    params.setVersionId(request.getVersionId());
    params.setPaginator(paginator);

    PageInfo<ItemSnapshot> snapshots = datasetAPI.listItemSnapshots(params);
    return snapshots.convert(ItemSnapshot::getSnapshot);
  }

  /**
   * 查找重复项
   */
  private List<String> findDuplicates(List<String> list) {
    Set<String> seen = new HashSet<>();
    return list.stream()
      .filter(item -> !seen.add(item))
      .distinct()
      .collect(Collectors.toList());
  }

  /**
   * 批量创建项目请求上下文
   */
  private record BatchCreateDatasetItemsReqContext(DatasetWithSchema dataset, List<IndexedItem> goodItems, List<ItemErrorGroup> badItems) {
  }
}
