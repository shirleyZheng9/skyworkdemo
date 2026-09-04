package com.iwhalecloud.bote.loop.evaluation.domain.service.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.IDatasetRPCAdapter;
import com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto.BatchCreateDatasetItemsParam;
import com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto.BatchGetDatasetItemsParam;
import com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto.ListDatasetItemsParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.BatchCreateEvaluationSetItemsParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.BatchCreateEvaluationSetItemsResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.BatchGetEvaluationSetItemsParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluationSetItem;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ListEvaluationSetItemsParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Turn;
import com.iwhalecloud.bote.loop.evaluation.domain.service.EvaluationSetItemService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 评估集项目服务实现类
 * 迁移对应关系: Go语言backend/modules/evaluation/domain/service/evaluation_set_item_impl.go
 * - 功能: 评估集项目业务逻辑实现
 * - 主要方法:
 * * batchCreateEvaluationSetItems - 批量创建评估集项目
 * * updateEvaluationSetItem - 更新评估集项目
 * * batchDeleteEvaluationSetItems - 批量删除评估集项目
 * * listEvaluationSetItems - 分页查询评估集项目列表
 * * batchGetEvaluationSetItems - 批量获取评估集项目
 * * clearEvaluationSetDraftItem - 清空评估集草稿项目
 * <p>
 * Java实现说明:
 * - 对应Go的EvaluationSetItemServiceImpl结构体
 * - 使用Spring Service注解
 * - 依赖数据集RPC适配器
 * - 统一异常处理
 * <p>
 * 技术栈迁移:
 * - Go context.Context -> Java方法参数
 * - Go error返回 -> Java异常处理
 * - Go sync.Once -> Java单例模式
 * - Go rpc.IDatasetRPCAdapter -> Java IDatasetRPCAdapter
 */
@Service
@RequiredArgsConstructor
public class EvaluationSetItemServiceImpl implements EvaluationSetItemService {

  private final IDatasetRPCAdapter datasetRPCAdapter;

  @Override
  public BatchCreateEvaluationSetItemsResult batchCreateEvaluationSetItems(BatchCreateEvaluationSetItemsParam param) {
    if (param == null) {
      throw new BssException("参数不能为空");
    }

    BatchCreateDatasetItemsParam rpcParam = BatchCreateDatasetItemsParam.builder()
      .spaceId(param.getSpaceId())
      .evaluationSetId(param.getEvaluationSetId())
      .items(param.getItems())
      .skipInvalidItems(param.getSkipInvalidItems())
      .allowPartialAdd(param.getAllowPartialAdd())
      .build();

    return datasetRPCAdapter.batchCreateDatasetItems(rpcParam);
  }

  @Override
  public void updateEvaluationSetItem(Long spaceId, Long evaluationSetId, Long itemId, List<Turn> turns) {
    datasetRPCAdapter.updateDatasetItem(spaceId, evaluationSetId, itemId, turns);
  }

  @Override
  public void batchDeleteEvaluationSetItems(Long spaceId, Long evaluationSetId, List<Long> itemIds) {
    datasetRPCAdapter.batchDeleteDatasetItems(spaceId, evaluationSetId, itemIds);
  }

  @Override
  public PageInfo<EvaluationSetItem> listEvaluationSetItems(ListEvaluationSetItemsParam param) {
    if (param == null) {
      throw new BssException("参数不能为空");
    }
    ListDatasetItemsParam listParam = ListDatasetItemsParam.builder()
      .spaceId(param.getSpaceId())
      .evaluationSetId(param.getEvaluationSetId())
      .versionId(param.getVersionId())
      .pageNumber(param.getPageNumber())
      .pageSize(param.getPageSize())
      .pageToken(param.getPageToken())
      .orderBys(param.getOrderBys())
      .itemIdsNotIn(param.getItemIdsNotIn())
      .build();
    PageInfo<EvaluationSetItem> evaluationSetItems = param.getVersionId() == null ? datasetRPCAdapter.listDatasetItems(listParam) : datasetRPCAdapter.listDatasetItemsByVersion(listParam);
    if (evaluationSetItems == null) {
      evaluationSetItems = new PageInfo<>();
    }
    return evaluationSetItems;
  }

  @Override
  public PageInfo<EvaluationSetItem> batchGetEvaluationSetItems(BatchGetEvaluationSetItemsParam param) {
    if (param == null) {
      throw new BssException("参数不能为空");
    }

    BatchGetDatasetItemsParam listParam = BatchGetDatasetItemsParam.builder()
      .spaceId(param.getSpaceId())
      .evaluationSetId(param.getEvaluationSetId())
      .itemIds(param.getItemIds())
      .versionId(param.getVersionId())
      .build();

    if (param.getVersionId() == null) {
      return datasetRPCAdapter.batchGetDatasetItems(listParam);
    }
    else {
      return datasetRPCAdapter.batchGetDatasetItemsByVersion(listParam);
    }
  }

  @Override
  public void clearEvaluationSetDraftItem(Long spaceId, Long evaluationSetId) {
    datasetRPCAdapter.clearEvaluationSetDraftItem(spaceId, evaluationSetId);
  }
}
