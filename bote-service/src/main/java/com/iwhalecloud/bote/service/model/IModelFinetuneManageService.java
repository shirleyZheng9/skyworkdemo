package com.iwhalecloud.bote.service.model;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.base.query.RecordQueryParams;
import com.iwhalecloud.bote.dto.model.EvalPublishRecordDTO;
import com.iwhalecloud.bote.dto.model.FinetunePublishRecordDTO;
import com.iwhalecloud.bote.dto.model.ModelEvalDTO;
import com.iwhalecloud.bote.dto.model.ModelFinetuneDTO;
import com.iwhalecloud.bote.dto.model.query.ModelFinetuneQueryParams;
import com.iwhalecloud.bote.dto.model.response.PredictResponse.PredictInfo;
import com.iwhalecloud.bote.dto.model.response.UpdatePublishStatusResponse;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.List;

/**
 * 模型微调管理服务
 *
 * @author auto
 * @since 2025-02-19
 */
public interface IModelFinetuneManageService {
  /**
   * 查询单个模型微调
   *
   * @param id 模型微调主键
   * @return 模型微调
   */
  ModelFinetuneDTO getFinetune(Long id);

  /**
   * 保存模型微调
   *
   * @param finetune 模型微调
   * @return 结果
   */
  ResultVO<ModelFinetuneDTO> saveFinetune(ModelFinetuneDTO finetune);

  /**
   * 删除模型微调
   *
   * @param id 模型微调主键
   * @return 结果
   */
  ResultVO<Void> deleteFinetune(Long id);

  /**
   * 上下架微调
   *
   * @param tenantId 租户 ID
   * @param id 主键
   * @param status 上下架状态
   * @return 结果
   */
  ResultVO<Void> publishFinetune(Long tenantId, Long id, String status);

  /**
   * 查询模型微调列表（分页）
   *
   * @param queryParams 查询条件
   * @return 模型微调分页列表
   */
  PageInfo<ModelFinetuneDTO> queryFinetunePage(ModelFinetuneQueryParams queryParams);

  /**
   * 查询模型微调列表
   *
   * @param queryParams 查询条件
   * @return 模型微调列表
   */
  List<ModelFinetuneDTO> queryFinetuneList(ModelFinetuneQueryParams queryParams);

  /**
   * 更新微调评测状态
   *
   * @param response 微调评测结果
   * @return 结果
   */
  ResultVO<Void> updatePublishStatus(UpdatePublishStatusResponse response);

  /**
   * 通知部署所有启用的微调模型
   *
   * @return 结果
   */
  ResultVO<Void> loadAllFinetun();

  /**
   * 查询微调任务（分页）
   *
   * @param params 查询条件
   * @return 微调任务（分页）
   */
  PageInfo<FinetunePublishRecordDTO> queryFinetuneRecordPage(RecordQueryParams params);

  /**
   * 保存模型评测
   *
   * @param eval 模型评测
   * @return 结果
   */
  ResultVO<ModelEvalDTO> saveEval(ModelEvalDTO eval);

  /**
   * 删除模型评测
   *
   * @param id 模型评测主键
   * @return 结果
   */
  ResultVO<Void> deleteEval(Long id);

  /**
   * 查询评测任务（分页）
   *
   * @param params 查询条件
   * @return 评测任务（分页）
   */
  PageInfo<EvalPublishRecordDTO> queryEvalRecordPage(RecordQueryParams params);

  /**
   * 中断发布流程
   *
   * @param publishId 记录 ID
   * @param type 类型
   * @return 结果
   */
  ResultVO<Void> cancel(Long publishId, String type);

  /**
   * 重试发布流程
   *
   * @param tenantId 租户 ID
   * @param publishId 记录 ID
   * @param type 类型
   * @return 结果
   */
  ResultVO<Void> retry(Long tenantId, Long publishId, String type);

  /**
   * 微调模型推理测试
   *
   * @param finetune 微调模型
   * @return 结果
   */
  ResultVO<PredictInfo> predict(ModelFinetuneDTO finetune);
}
