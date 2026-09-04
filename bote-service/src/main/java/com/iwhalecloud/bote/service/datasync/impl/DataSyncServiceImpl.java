package com.iwhalecloud.bote.service.datasync.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.cache.TableDefinitionCache;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.enums.PublishStepType;
import com.iwhalecloud.bote.common.thread.ThreadPools;
import com.iwhalecloud.bote.dto.base.CatalogDTO;
import com.iwhalecloud.bote.dto.base.PublishRecordDTO;
import com.iwhalecloud.bote.dto.base.SimpleElementDTO;
import com.iwhalecloud.bote.dto.base.query.CatalogQueryParams;
import com.iwhalecloud.bote.dto.datasync.DataSyncGroupDTO;
import com.iwhalecloud.bote.dto.datasync.DataSyncNodeDTO;
import com.iwhalecloud.bote.dto.datasync.DataSyncTableDefinition;
import com.iwhalecloud.bote.dto.datasync.query.CopyDataParams;
import com.iwhalecloud.bote.dto.datasync.query.DataSyncParams;
import com.iwhalecloud.bote.dto.datasync.query.DataSyncQueryParams;
import com.iwhalecloud.bote.dto.datasync.query.ExportDataParams;
import com.iwhalecloud.bote.dto.datasync.query.ImportDataParams;
import com.iwhalecloud.bote.dto.datasync.query.OnlinePublishParams;
import com.iwhalecloud.bote.mapper.base.CatalogManageMapper;
import com.iwhalecloud.bote.mapper.base.DataSyncRecordManageMapper;
import com.iwhalecloud.bote.mapper.base.PublishManageMapper;
import com.iwhalecloud.bote.service.datasync.IDataSyncService;
import com.iwhalecloud.bote.service.datasync.helper.QueryDataRecordHelper;
import com.iwhalecloud.bote.service.datasync.util.DataSyncDirUtil;
import com.iwhalecloud.bote.service.element.IResourceElementService;
import com.iwhalecloud.bote.service.publish.IPublishService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.database.inspect.definition.Table;
import com.iwhalecloud.bss.litchi.database.util.TransactionUtil;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * 数据导出、导出服务实现
 *
 * @author chen.linfa
 * @since 2024-10-21
 */
@Service
@RequiredArgsConstructor
public class DataSyncServiceImpl implements IDataSyncService {

  // @formatter:off
  private final DataSyncRecordManageMapper mapper;
  private final PublishManageMapper publishMapper;
  private final CatalogManageMapper catalogManageMapper;
  private final TableDefinitionCache tableDefinitionCache;
  private final QueryDataRecordHelper queryDataRecordHelper;
  private final IPublishService publishService;
  private final IResourceElementService resourceElementService;
  // @formatter:on
  @Override
  @SuppressWarnings("PMD.PreserveStackTrace")
  public ResultVO<Long> publishExport(ExportDataParams params) {
    return publishExport(params, BaseConsts.PUBLISH_TYPE_EXPORT);
  }

  /**
   * 发布导出数据流程，可指定发布类型
   *
   * @param params 入参
   * @param publishType 发布类型
   * @return 结果
   */
  @Override
  @SuppressWarnings("PMD.PreserveStackTrace")
  public ResultVO<Long> publishExport(ExportDataParams params, String publishType) {
    boolean hasRunningReturn = publishMapper.existsRunningRecord(params.getTenantId(), BaseConsts.PUBLISH_TYPE_RETURN);
    if (hasRunningReturn) {
      throw new BssException("存在进行中的回退任务，请稍后再执行，可以查询回退记录列表查看详情");
    }
    // 创建发布
    List<PublishStepType> steps = Arrays.asList(PublishStepType.INITIALIZE_EXPORT, PublishStepType.COllECT_DATA, PublishStepType.WRAP_EXPORT);
    PublishRecordDTO record = publishService.buildRecord(publishType, steps);
    record.setTenantId(params.getTenantId());
    record.setInput(JsonUtil.toJsonString(params));

    // 独立事务，保存到数据库
    TransactionUtil.executeNew(() -> {
      publishMapper.insertRecord(record);
      publishMapper.batchInsertStep(record.getSteps());
    });

    // 执行发布
    Future<?> future = ThreadPools.getCommon().submit(() -> publishService.start(record.getId(), true, params));
    try {
      // 等待 3s, 以便发现部分错误。如果未完成，不再等待，但执行流程还在继续
      future.get(3, TimeUnit.SECONDS);
    }
    catch (TimeoutException e) {
      // ignore timeout
    }
    catch (ExecutionException e) {
      throw new BssException("步骤化数据导出异常，" + e.getMessage(), e);
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new BssException("步骤化数据导出异常，" + e.getMessage(), e);
    }
    return ResultVO.success(record.getId());
  }

  @Override
  @SuppressWarnings("PMD.PreserveStackTrace")
  public ResultVO<Long> publishImport(ImportDataParams params) {
    return publishImport(params, BaseConsts.PUBLISH_TYPE_IMPORT);
  }

  /**
   * 发布导入数据流程，可指定发布类型
   *
   * @param params 入参
   * @param publishType 发布类型
   * @return 结果
   */
  @Override
  @SuppressWarnings("PMD.PreserveStackTrace")
  public ResultVO<Long> publishImport(ImportDataParams params, String publishType) {
    Long ownerId = params.getTenantId() != null ? params.getTenantId() : params.getSpaceId();
    validateNoRunningDataSyncPublish(ownerId);
    // 创建发布
    List<PublishStepType> steps = Arrays.asList(PublishStepType.PARSE_FILE, PublishStepType.CHANGE_ENV_INST, PublishStepType.CONFIRM_IMPORT);
    PublishRecordDTO record = publishService.buildRecord(publishType, steps);
    record.setTenantId(ownerId);
    record.setInput(JsonUtil.toJsonString(params));

    // 独立事务，保存到数据库
    TransactionUtil.executeNew(() -> {
      publishMapper.insertRecord(record);
      publishMapper.batchInsertStep(record.getSteps());
    });

    // 执行发布
    Future<?> future = ThreadPools.getCommon().submit(() -> publishService.start(record.getId(), true, params));
    try {
      // 等待 3s, 以便发现部分错误。如果未完成，不再等待，但执行流程还在继续
      future.get(3, TimeUnit.SECONDS);
    }
    catch (TimeoutException e) {
      // ignore timeout
    }
    catch (ExecutionException e) {
      throw new BssException("步骤化数据导入异常，" + e.getMessage(), e);
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new BssException("步骤化数据导入异常，" + e.getMessage(), e);
    }
    return ResultVO.success(record.getId());
  }

  /**
   * 校验数据同步相关操作是否存在运行中的互斥流程。
   *
   * @param tenantId 租户 ID
   */
  private void validateNoRunningDataSyncPublish(Long tenantId) {
    if (tenantId == null) {
      return;
    }
    boolean hasRunningImport = publishMapper.existsRunningRecord(tenantId, BaseConsts.PUBLISH_TYPE_IMPORT);
    Assert.isTrue(!hasRunningImport, "存在进行中的导入任务，请稍后再执行，可以查询导入记录列表查看详情");
    boolean hasRunningBackup = publishMapper.existsRunningRecord(tenantId, BaseConsts.PUBLISH_TYPE_BACKUP);
    Assert.isTrue(!hasRunningBackup, "存在进行中的备份任务，请稍后再执行，可以查询备份列表查看详情");
    boolean hasRunningReturn = publishMapper.existsRunningRecord(tenantId, BaseConsts.PUBLISH_TYPE_RETURN);
    Assert.isTrue(!hasRunningReturn, "存在进行中的回退任务，请稍后再执行，可以查询回退记录列表查看详情");
  }

  @Override
  @SuppressWarnings("PMD.PreserveStackTrace")
  public ResultVO<Long> publishCopy(CopyDataParams params) {
    // 创建发布
    List<PublishStepType> steps = Arrays.asList(PublishStepType.INITIALIZE_FOR_COPY, PublishStepType.COllECT_DATA_FOR_COPY, PublishStepType.SAVE_DATA_FOR_COPY);
    PublishRecordDTO record = publishService.buildRecord(BaseConsts.PUBLISH_TYPE_COPY, steps);
    record.setTenantId(params.getResetTenantId());
    record.setObjId(params.getTenantId());
    record.setInput(JsonUtil.toJsonString(params));

    // 独立事务，保存到数据库
    TransactionUtil.executeNew(() -> {
      publishMapper.insertRecord(record);
      publishMapper.batchInsertStep(record.getSteps());
    });

    // 执行发布
    Future<?> future = ThreadPools.getCommon().submit(() -> publishService.start(record.getId(), true, params));
    try {
      // 等待 3s, 以便发现部分错误。如果未完成，不再等待，但执行流程还在继续
      future.get(3, TimeUnit.SECONDS);
    }
    catch (TimeoutException e) {
      // ignore timeout
    }
    catch (ExecutionException e) {
      throw new BssException("步骤化数据复制异常，" + e.getMessage(), e);
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new BssException("步骤化数据复制异常，" + e.getMessage(), e);
    }
    return ResultVO.success(record.getId());
  }

  @Override
  @SuppressWarnings("PMD.PreserveStackTrace")
  public ResultVO<Long> publishOnline(OnlinePublishParams params) {
    // 创建发布
    List<PublishStepType> steps = Arrays.asList(PublishStepType.INITIALIZE_ONLINE, PublishStepType.COLLECT_DATA_ONLINE,
      PublishStepType.CONFIRM_PUBLISH_ONLINE);
    PublishRecordDTO record = publishService.buildRecord(BaseConsts.PUBLISH_TYPE_ONLINE, steps);
    record.setTenantId(params.getTenantId());
    record.setInput(JsonUtil.toJsonString(params));

    // 独立事务，保存到数据库
    TransactionUtil.executeNew(() -> {
      publishMapper.insertRecord(record);
      publishMapper.batchInsertStep(record.getSteps());
    });

    // 执行发布
    Future<?> future = ThreadPools.getCommon().submit(() -> publishService.start(record.getId(), true, params));
    try {
      // 等待 3s, 以便发现部分错误。如果未完成，不再等待，但执行流程还在继续
      future.get(3, TimeUnit.SECONDS);
    }
    catch (TimeoutException e) {
      // ignore timeout
    }
    catch (ExecutionException e) {
      throw new BssException("在线数据发布异常，" + e.getMessage(), e);
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new BssException("在线数据发布异常，" + e.getMessage(), e);
    }
    return ResultVO.success(record.getId());
  }

  @Override
  public List<DataSyncTableDefinition> queryTableDefinition() {
    List<DataSyncTableDefinition> definitions = mapper.selectTableDefinition();
    for (DataSyncTableDefinition definition : definitions) {
      Table table = tableDefinitionCache.get(definition.getTableCode());
      Assert.notNull(table, definition.getTableCode() + "表定义不存在。请检查数据库是否存在该表，如果存在该表，请刷新平台缓存");
      // 字段编码统一转成小写格式，方便后续的数据计算
      definition.setTableColumns(
        CollectionUtils.emptyIfNull(table.getColumns()).stream().map(m -> m.getName().toLowerCase()).collect(Collectors.toList()));
      // 对于时间类型的字段，需要打上标记，特殊处理。目的：导入导出时间字段值格式转换，用统一的处理方式，兼容不同数据库类型
      List<String> dateColumns = CollectionUtils.emptyIfNull(table.getColumns()).stream()
        .filter(p -> BaseConsts.DATE_TIME_DATA_TYPE.contains(p.getDataType())).map(m -> m.getName().toLowerCase()).collect(Collectors.toList());
      definition.setDateColumns(dateColumns);
      definition.setTableCode(table.getName().toLowerCase());
      definition.setPrimaryKey(definition.getPrimaryKey().toLowerCase());
      definition.setForeignKey(StringUtils.isEmpty(definition.getForeignKey()) ? "" : definition.getForeignKey().toLowerCase());
    }
    return definitions;
  }

  @Override
  public ResultVO<Void> parseImportFile(DataSyncParams params, File tempFile) {
    DataSyncParams target = DataSyncDirUtil.uploadZip(tempFile);
    if (target == null) {
      return ResultVO.fail("导入数据包内容错误");
    }
    params.setCompressDir(target.getCompressDir());
    params.setDecompressDir(target.getDecompressDir());
    params.setTenantId(target.getTenantId());
    params.setSyncAll(target.getSyncAll());
    params.setCodeAndIds(target.getCodeAndIds());
    params.setDefinitions(target.getDefinitions());
    return ResultVO.success();
  }

  @Override
  public List<DataSyncGroupDTO> queryDataSyncNode(DataSyncQueryParams queryParams) {
    Map<String, List<DataSyncNodeDTO>> group = mapper.selectDataSyncNode().stream().collect(Collectors.groupingBy(DataSyncNodeDTO::getParentCode));
    List<DataSyncGroupDTO> list = new ArrayList<>();
    CatalogQueryParams params = new CatalogQueryParams();
    params.setTenantId(queryParams.getTenantId());
    Map<String, List<CatalogDTO>> catalogs = catalogManageMapper.selectCatalogList(params).stream()
      .collect(Collectors.groupingBy(CatalogDTO::getCatalogType));
    for (Entry<String, List<DataSyncNodeDTO>> entry : group.entrySet()) {
      int total = 0;
      for (DataSyncNodeDTO node : entry.getValue()) {
        queryDataRecordHelper.setDataRecord(queryParams.getTenantId(), queryParams.getStartDate(), node, catalogs.get(node.getCatalogType()));
        total = total + node.getTotal();
      }
      // 例外空数据节点
      if (total > 0) {
        DataSyncGroupDTO parent = new DataSyncGroupDTO();
        parent.setCode(entry.getKey());
        parent.setName(entry.getValue().get(0).getParentName());
        parent.setNodes(entry.getValue());
        parent.setTotal(total);
        parent.setGroupSortby(entry.getValue().get(0).getGroupSortby());
        list.add(parent);
      }
    }
    list.sort(Comparator.comparing(DataSyncGroupDTO::getGroupSortby));
    return list;
  }

  @Override
  public ResultVO<List<DataSyncNodeDTO>> queryRelatedResource(DataSyncQueryParams queryParams) {
    Map<String, List<SimpleElementDTO>> data = resourceElementService.queryRelatedResource(queryParams.getTenantId(), queryParams.getValues(),
      queryParams.getCode());
    if (MapUtils.isEmpty(data)) {
      return ResultVO.success();
    }
    // 按照增量导出面板，剔除多余的关联节点
    List<DataSyncNodeDTO> nodes = mapper.selectDataSyncNode();
    List<DataSyncNodeDTO> list = new ArrayList<>();
    for (Entry<String, List<SimpleElementDTO>> entry : data.entrySet()) {
      DataSyncNodeDTO node = IterableUtils.find(nodes, p -> p.getCode().equals(entry.getKey()));
      if (node == null) {
        continue;
      }
      // 根据 id 字段去重
      List<SimpleElementDTO> uniqueElements = new ArrayList<>(
        entry.getValue().stream().collect(Collectors.toMap(SimpleElementDTO::getId, element -> element, (existing, replacement) -> existing))
          .values());
      node.setRecords(JsonUtil.parseJsonRequired(JsonUtil.toJsonString(uniqueElements), new TypeReference<List<Map<String, Object>>>() {
      }));
      list.add(node);
    }
    return ResultVO.success(list);
  }
}
