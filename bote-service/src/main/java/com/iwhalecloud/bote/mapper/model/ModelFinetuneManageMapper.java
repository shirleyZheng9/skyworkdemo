package com.iwhalecloud.bote.mapper.model;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.model.ModelEvalDTO;
import com.iwhalecloud.bote.dto.model.ModelFinetuneDTO;
import com.iwhalecloud.bote.dto.model.SimpleIntentFinetuneDTO;
import com.iwhalecloud.bote.dto.model.query.ModelFinetuneQueryParams;
import java.math.BigDecimal;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * 模型微调管理
 *
 * @author auto
 * @since 2024-12-19
 */
public interface ModelFinetuneManageMapper {
  /**
   * 校验模型微调的唯一性
   *
   * @param finetune 模型微调
   * @return 结果
   */
  boolean existsFinetuneName(@Param("dto") ModelFinetuneDTO finetune);

  /**
   * 根据主键获取模型微调
   *
   * @param id 模型微调主键
   * @return 模型微调
   */
  ModelFinetuneDTO getFinetune(@Param("id") Long id);

  /**
   * 新增模型微调
   *
   * @param finetune 模型微调
   * @return 结果
   */
  int insertFinetune(@Param("dto") ModelFinetuneDTO finetune);

  /**
   * 修改模型微调
   *
   * @param finetune 模型微调
   * @return 结果
   */
  int updateFinetune(@Param("dto") ModelFinetuneDTO finetune);

  /**
   * 删除模型微调
   *
   * @param id 主键 ID
   * @param updatorId 操作人 ID
   * @return 结果
   */
  int deleteFinetune(@Param("id") Long id, @Param("updatorId") Long updatorId);

  /**
   * 下线租户下的意图用途微调
   *
   * @param tenantId 租户 ID
   * @param updatorId 修改人
   * @return 结果
   */
  int updateFinetuneStatusByTenantId(@Param("tenantId") Long tenantId, @Param("updatorId") Long updatorId);

  /**
   * 更新微调上下架状态
   */
  int updateFinetuneStatus(@Param("tenantId") Long tenantId, @Param("id") Long id, @Param("updatorId") Long updatorId, @Param("status") String status);

  /**
   * 获取模型微调列表（分页）
   *
   * @param queryParams 查询条件
   * @return 模型微调分页列表
   */
  Page<ModelFinetuneDTO> selectFinetunePage(@Param("query") ModelFinetuneQueryParams queryParams, RowBounds rowBounds);

  /**
   * 获取模型微调列表
   *
   * @param queryParams 查询条件
   * @return 模型微调列表
   */
  List<ModelFinetuneDTO> selectFinetuneList(@Param("query") ModelFinetuneQueryParams queryParams);

  /**
   * 查询租户已上架的微调
   *
   * @param tenantId 租户 ID
   * @return 微调
   */
  SimpleIntentFinetuneDTO getOnlineFinetune(@Param("tenantId") Long tenantId);

  /**
   * 查询已上架的微调
   *
   * @return 微调模型
   */
  List<SimpleIntentFinetuneDTO> selectOnlineFinetune();

  /**
   * 根据主键查询评测
   *
   * @param id 主键 ID
   * @return 评测
   */
  ModelEvalDTO getEval(@Param("id") Long id);

  /**
   * 新增模型评测
   *
   * @param eval 模型评测
   * @return 结果
   */
  int insertEval(@Param("dto") ModelEvalDTO eval);

  /**
   * 删除模型评测
   *
   * @param id 主键 ID
   * @param updatorId 操作人 ID
   * @return 结果
   */
  int deleteEval(@Param("id") Long id, @Param("updatorId") Long updatorId);

  /**
   * 更新评测结果
   *
   * @param id 主键
   * @param status 状态
   * @param accuracy 准确率
   * @return 结果
   */
  int updateEvalResult(@Param("id") Long id, @Param("status") String status, @Param("accuracy") BigDecimal accuracy);
}
