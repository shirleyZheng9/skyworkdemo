package com.iwhalecloud.bote.mapper.model;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.model.LargeModelDTO;
import com.iwhalecloud.bote.dto.model.SimpleLargeModelDTO;
import com.iwhalecloud.bote.dto.model.query.LargeModelQueryParams;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;
import org.springframework.lang.Nullable;

/**
 * 大模型管理
 *
 * @author auto
 * @since 2024-09-20
 */
public interface LargeModelManageMapper {
  /**
   * 校验大模型的编码唯一性
   *
   * @param model 大模型
   * @return 结果
   */
  boolean existsLargeModelCode(@Param("dto") LargeModelDTO model);

  /**
   * 根据主键获取大模型
   *
   * @param tenantId 租户 ID
   * @param modelId 大模型主键
   * @return 大模型
   */
  LargeModelDTO getLargeModel(@Param("tenantId") Long tenantId, @Param("id") Long modelId);

  /**
   * 根据主键获取大模型（不限制 status_cd，网关同步恢复用）
   *
   * @param tenantId 租户 ID
   * @param modelId 大模型主键
   * @return 大模型
   */
  LargeModelDTO getLargeModelIgnoreStatus(@Param("tenantId") Long tenantId, @Param("id") Long modelId);

  /**
   * 新增大模型
   *
   * @param model 大模型
   * @return 结果
   */
  int insertLargeModel(@Param("dto") LargeModelDTO model);

  /**
   * 修改大模型
   *
   * @param model 大模型
   * @return 结果
   */
  int updateLargeModel(@Param("dto") LargeModelDTO model);

  /**
   * 删除属性
   *
   * @param tenantId 租户 ID
   * @param modelId 主键 ID
   * @param updatorId 操作人 ID
   * @return 结果
   */
  int deleteLargeModel(@Param("tenantId") Long tenantId, @Param("modelId") Long modelId, @Param("updatorId") Long updatorId);

  /**
   * 获取大模型列表（分页）
   *
   * @param queryParams 查询条件
   * @return 大模型分页列表
   */
  Page<LargeModelDTO> selectLargeModelPage(@Param("query") LargeModelQueryParams queryParams, RowBounds rowBounds);

  /**
   * 查询大模型列表
   *
   * @param tenantId 租户 ID
   * @param modelType 模型分类，可选
   * @param sourceFrom 产品来源，可选
   * @return 大模型列表
   */
  List<SimpleLargeModelDTO> selectLargeModelList(@Param("tenantId") Long tenantId, @Param("modelType") String modelType, @Param("sourceFrom") String sourceFrom);

  /**
   * 查询大模型列表
   *
   * @param tenantId 租户 ID
   * @param botId 应用 ID
   * @param modelType 模型分类，可选
   * @return 大模型列表
   */
  List<SimpleLargeModelDTO> selectAiLargeModelList(@Param("tenantId") Long tenantId, @Param("botId") Long botId, @Param("modelType") String modelType, @Param("userId") Long userId);

  /**
   * 根据 ID 查询大模型配置
   *
   * @param tenantId 租户 ID
   * @param modelId 大模型 ID
   * @return 大模型配置
   */
  @Nullable
  SimpleLargeModelDTO selectLargeModelById(@Param("tenantId") Long tenantId, @Param("modelId") Long modelId);

  /**
   * @param modelId 模型id
   * @return 返回图标
   */
  String getLargeModelIcon(@Param("modelId") Long modelId, @Param("tenantId") Long tenantId);

  /**
   * 获取个人大模型列表（分页）
   *
   * @param queryParams 查询条件
   * @return 大模型分页列表
   */
  Page<LargeModelDTO> selectPersonalLargeModelPage(@Param("query") LargeModelQueryParams queryParams, RowBounds rowBounds);

  /**
   * 更新大模型启用状态
   *
   * @param spaceId 空间 ID
   * @param modelId 模型 ID
   * @param enabled 启用状态
   * @param updatorId 操作人 ID
   * @return 结果
   */
  int updateLargeModelEnabledStatus(@Param("spaceId") Long spaceId, @Param("modelId") Long modelId,
                                    @Param("enabled") String enabled, @Param("updatorId") Long updatorId);

  /**
   * 获取已启用的大模型（不限制维度）
   *
   * @param tenantId 租户 ID
   * @return 已启用的大模型
   */
  SimpleLargeModelDTO selectEnabledLargeModelIgnoreDataFrom(@Param("tenantId") Long tenantId);

  /**
   * 获取已启用的大模型（适用于平台和空间维度）
   *
   * @param tenantId 租户 ID
   * @return 已启用的大模型
   */
  SimpleLargeModelDTO selectEnabledLargeModel(@Param("tenantId") Long tenantId);

  /**
   * 查询租户下来自天工 AI 网关同步的大模型
   *
   * @param tenantId 租户 ID
   * @return 网关来源大模型列表
   */
  List<LargeModelDTO> selectGatewayLargeModels(@Param("tenantId") Long tenantId);
}
