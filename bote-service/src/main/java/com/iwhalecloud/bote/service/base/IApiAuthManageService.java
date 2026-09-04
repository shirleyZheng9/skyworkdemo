package com.iwhalecloud.bote.service.base;

import com.iwhalecloud.bote.dto.base.ApiAuthDTO;
import com.iwhalecloud.bote.dto.base.ApiDTO;
import com.iwhalecloud.bote.dto.base.AppPublishDTO;
import com.iwhalecloud.bote.dto.base.CatalogTree;
import com.iwhalecloud.bote.dto.base.query.ApiAuthQueryParams;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.List;
import org.springframework.lang.Nullable;

/**
 * API 鉴权管理服务
 *
 * @author auto
 * @since 2024-09-19
 */
public interface IApiAuthManageService {

  /**
   * 查询单个鉴权
   *
   * @param tenantId 租户 ID
   * @param authId 鉴权主键
   * @return 鉴权
   */
  @Nullable
  ApiAuthDTO getApiAuth(Long tenantId, Long authId);

  /**
   * 保存鉴权
   *
   * @param apiAuth 鉴权
   * @return 结果
   */
  ResultVO<ApiAuthDTO> saveApiAuth(ApiAuthDTO apiAuth);

  /**
   * 删除鉴权
   *
   * @param tenantId 租户 ID
   * @param authId 鉴权主键
   * @return 结果
   */
  ResultVO<Void> deleteApiAuth(Long tenantId, Long authId);

  /**
   * 查询鉴权列表
   *
   * @param queryParams 查询条件
   * @return 鉴权列表
   */
  List<ApiAuthDTO> queryApiAuthList(ApiAuthQueryParams queryParams);

  /**
   * 查询 API 列表
   *
   * @return API 列表
   */
  List<CatalogTree<ApiDTO>> selectApiList();

  /**
   * 保存智能体发布信息
   *
   * @param publish 发布
   * @return 结果
   */
  ResultVO<AppPublishDTO> saveAppPublish(AppPublishDTO publish);

  /**
   * 删除智能体发布信息
   *
   * @param publishId 主键
   * @return 结果
   */
  ResultVO<Void> deleteAppPublish(Long publishId);

  /**
   * 查询智能体发布列表
   *
   * @param tenantId 租户 ID
   * @return 发布列表
   */
  List<AppPublishDTO> queryAppPublishList(Long tenantId, String botName, Long botId);
}
