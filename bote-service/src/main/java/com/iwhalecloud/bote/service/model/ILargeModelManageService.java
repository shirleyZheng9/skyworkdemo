package com.iwhalecloud.bote.service.model;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.model.GatewayModelSyncResult;
import com.iwhalecloud.bote.dto.model.LargeModelDTO;
import com.iwhalecloud.bote.dto.model.SimpleLargeModelDTO;
import com.iwhalecloud.bote.dto.model.query.LargeModelQueryParams;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.List;
import java.util.Map;

import org.springframework.lang.Nullable;

/**
 * 大模型管理服务
 *
 * @author auto
 * @since 2024-09-20
 */
public interface ILargeModelManageService {

  /**
   * 查询单个大模型
   *
   * @param tenantId 租户 ID
   * @param modelId 大模型主键
   * @return 大模型
   */
  LargeModelDTO getLargeModel(Long tenantId, Long modelId);

  /**
   * 保存大模型
   *
   * @param model 大模型
   * @return 结果
   */
  ResultVO<LargeModelDTO> saveLargeModel(LargeModelDTO model);

  /**
   * 删除大模型
   *
   * @param tenantId 租户 ID
   * @param modelId 大模型主键
   * @return 结果
   */
  ResultVO<Void> deleteLargeModel(Long tenantId, Long modelId);

  /**
   * 查询大模型列表（分页）
   *
   * @param queryParams 查询条件
   * @return 大模型分页列表
   */
  PageInfo<LargeModelDTO> queryLargeModelPage(LargeModelQueryParams queryParams);

  /**
   * 查询大模型列表
   *
   * @param tenantId 租户 ID
   * @param modelType 模型分类
   * @return 大模型列表
   */
  List<SimpleLargeModelDTO> queryLargeModelList(Long tenantId, @Nullable String modelType, @Nullable String sourceFrom);

  /**
   * 查询大模型列表
   *
   * @param tenantId 租户 ID
   * @param botId 应用 ID
   * @param modelType 模型分类
   * @return 大模型列表
   */
  List<SimpleLargeModelDTO> queryAiLargeModelList(Long tenantId, Long botId, @Nullable String modelType);

  /**
   * 获取租户的默认大模型
   *
   * @param tenantId 租户 ID
   * @return 结果
   */
  @Nullable
  Long getDefaultLargeModel(Long tenantId);

  /**
   * 设置租户的默认大模型
   *
   * @param tenantId 租户 ID
   * @param modelId 模型ID
   */
  void setDefaultLargeModel(Long tenantId, Long modelId);

  String getLargeModelIcon(Long modelId, Long tenantId);

  /**
   * 修改个人大模型启用状态
   *
   * @param spaceId 空间ID
   * @param modelId 模型ID
   * @param enabled 启用状态
   */
  void modifyPersonalLargeModelStatus(Long spaceId, Long modelId, String enabled);

  /**
   * 查询个人大模型列表（分页）
   *
   * @param queryParams 查询条件
   * @return 大模型分页列表
   */
  PageInfo<LargeModelDTO> queryPersonalLargeModelPage(LargeModelQueryParams queryParams);

  /**
   * 修改大模型启用状态
   *
   * @param tenantId 租户 ID
   * @param modelId 模型ID
   * @param enabled 启用状态
   */
  void modifyLargeModelStatus(Long tenantId, Long modelId, String enabled);

  /**
   * 获取已启用的大模型
   *
   * @param tenantId 租户 ID
   * @return 大模型简略数据
   */
  @Nullable
  SimpleLargeModelDTO queryEnabledLargeModel(Long tenantId);

  /**
   * 获取已启用的平台大模型和空间大模型
   *
   * @param spaceTenantId 空间虚拟租户 ID
   * @return 平台大模型和空间大模型map
   */
  Map<String, SimpleLargeModelDTO> queryPlatformAndSpaceEnabledLargeModel(@Nullable Long spaceTenantId);

  /**
   * 从天工 AI 网关同步模型到项目租户
   *
   * @param tenantId 项目租户 ID
   * @param spaceId Bote 空间 ID（内部映射 extSpaceId 后查 AI Key）
   * @return 同步结果
   */
  GatewayModelSyncResult syncGatewayModels(Long tenantId, Long spaceId);
}
