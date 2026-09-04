package com.iwhalecloud.bote.loop.client.evaluation.eval_set;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_set.EvaluationSetDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_set.EvaluationSetItemDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_set.EvaluationSetVersionDTO;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.BatchCreateEvaluationSetItemsRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.BatchCreateEvaluationSetItemsResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.BatchDeleteEvaluationSetItemsRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.BatchDeleteEvaluationSetItemsResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.BatchGetEvaluationSetItemsRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.BatchGetEvaluationSetItemsResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.BatchGetEvaluationSetVersionsRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.BatchGetEvaluationSetVersionsResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.ClearEvaluationSetDraftItemRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.ClearEvaluationSetDraftItemResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.CreateEvaluationSetRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.CreateEvaluationSetResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.CreateEvaluationSetVersionRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.CreateEvaluationSetVersionResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.DeleteEvaluationSetRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.DeleteEvaluationSetResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.GetEvaluationSetRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.GetEvaluationSetResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.GetEvaluationSetVersionRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.GetEvaluationSetVersionResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.ImportEvaluationSetItemsRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.ImportEvaluationSetItemsResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.ListEvaluationSetItemsRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.ListEvaluationSetVersionsRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.ListEvaluationSetsRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.UpdateEvaluationSetItemRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.UpdateEvaluationSetItemResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.UpdateEvaluationSetRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.UpdateEvaluationSetResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.UpdateEvaluationSetSchemaRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.UpdateEvaluationSetSchemaResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * 评测集服务接口
 * 对应Thrift: EvaluationSetService
 */
public interface EvaluationSetApplicationService {

  /**
   * 创建评测集
   * 对应Thrift方法: CreateEvaluationSet
   */
  CreateEvaluationSetResponse createEvaluationSet(CreateEvaluationSetRequest request);

  /**
   * 更新评测集
   * 对应Thrift方法: UpdateEvaluationSet
   */
  UpdateEvaluationSetResponse updateEvaluationSet(UpdateEvaluationSetRequest request);

  /**
   * 删除评测集
   * 对应Thrift方法: DeleteEvaluationSet
   */
  DeleteEvaluationSetResponse deleteEvaluationSet(DeleteEvaluationSetRequest request);

  /**
   * 获取评测集
   * 对应Thrift方法: GetEvaluationSet
   */
  GetEvaluationSetResponse getEvaluationSet(GetEvaluationSetRequest request);

  /**
   * 列表评测集
   * 对应Thrift方法: ListEvaluationSets
   */
  PageInfo<EvaluationSetDTO> listEvaluationSets(ListEvaluationSetsRequest request);

  /**
   * 创建评测集版本
   * 对应Thrift方法: CreateEvaluationSetVersion
   */
  CreateEvaluationSetVersionResponse createEvaluationSetVersion(CreateEvaluationSetVersionRequest request);

  /**
   * 获取评测集版本
   * 对应Thrift方法: GetEvaluationSetVersion
   */
  GetEvaluationSetVersionResponse getEvaluationSetVersion(GetEvaluationSetVersionRequest request);

  /**
   * 列表评测集版本
   * 对应Thrift方法: ListEvaluationSetVersions
   */
  PageInfo<EvaluationSetVersionDTO> listEvaluationSetVersions(ListEvaluationSetVersionsRequest request);

  /**
   * 批量获取评测集版本
   * 对应Thrift方法: BatchGetEvaluationSetVersions
   */
  BatchGetEvaluationSetVersionsResponse batchGetEvaluationSetVersions(BatchGetEvaluationSetVersionsRequest request);

  /**
   * 更新评测集字段
   * 对应Thrift方法: UpdateEvaluationSetSchema
   */
  UpdateEvaluationSetSchemaResponse updateEvaluationSetSchema(UpdateEvaluationSetSchemaRequest request);

  /**
   * 批量创建评测集数据
   * 对应Thrift方法: BatchCreateEvaluationSetItems
   */
  BatchCreateEvaluationSetItemsResponse batchCreateEvaluationSetItems(BatchCreateEvaluationSetItemsRequest request);

  /**
   * 更新评测集数据
   * 对应Thrift方法: UpdateEvaluationSetItem
   */
  UpdateEvaluationSetItemResponse updateEvaluationSetItem(UpdateEvaluationSetItemRequest request);

  /**
   * 批量删除评测集数据
   * 对应Thrift方法: BatchDeleteEvaluationSetItems
   */
  BatchDeleteEvaluationSetItemsResponse batchDeleteEvaluationSetItems(BatchDeleteEvaluationSetItemsRequest request);

  /**
   * 列表评测集数据
   * 对应Thrift方法: ListEvaluationSetItems
   */
  PageInfo<EvaluationSetItemDTO> listEvaluationSetItems(ListEvaluationSetItemsRequest request);

  /**
   * 批量获取评测集数据
   * 对应Thrift方法: BatchGetEvaluationSetItems
   */
  BatchGetEvaluationSetItemsResponse batchGetEvaluationSetItems(BatchGetEvaluationSetItemsRequest request);

  /**
   * 清空评测集草稿数据
   * 对应Thrift方法: ClearEvaluationSetDraftItem
   */
  ClearEvaluationSetDraftItemResponse clearEvaluationSetDraftItem(ClearEvaluationSetDraftItemRequest request);


  ImportEvaluationSetItemsResponse batchImportEvaluationSetItems(MultipartFile file, ImportEvaluationSetItemsRequest request);
}
