package com.iwhalecloud.bote.mapper.base;

import com.iwhalecloud.bote.dto.base.ApiAuthDTO;
import com.iwhalecloud.bote.dto.base.ApiDTO;
import com.iwhalecloud.bote.dto.base.AppPublishDTO;
import com.iwhalecloud.bote.dto.base.SimpleApiAuthDTO;
import com.iwhalecloud.bote.dto.base.query.ApiAuthQueryParams;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * API 鉴权管理
 *
 * @author auto
 * @since 2024-09-19
 */
public interface ApiAuthManageMapper {

  /**
   * 根据密钥获取鉴权
   *
   * @param signature 密钥
   * @return 鉴权
   */
  SimpleApiAuthDTO getApiAuthByKey(@Param("signature") String signature);

  /**
   * 根据主键获取鉴权
   */
  ApiAuthDTO getApiAuth(@Param("tenantId") Long tenantId, @Param("id") Long authId);

  /**
   * 新增鉴权
   *
   * @param apiAuth 鉴权
   * @return 结果
   */
  int insertApiAuth(@Param("dto") ApiAuthDTO apiAuth);

  /**
   * 修改鉴权
   *
   * @param apiAuth 鉴权
   * @return 结果
   */
  int updateApiAuth(@Param("dto") ApiAuthDTO apiAuth);

  /**
   * 删除 API 鉴权
   */
  int deleteApiAuth(@Param("tenantId") Long tenantId, @Param("authId") Long authId, @Param("updatorId") Long updatorId);

  /**
   * 获取鉴权列表
   *
   * @param queryParams 查询条件
   * @return 鉴权列表
   */
  List<ApiAuthDTO> selectApiAuthList(@Param("query") ApiAuthQueryParams queryParams);

  /**
   * 获取 API 列表
   *
   * @return API 列表
   */
  List<ApiDTO> selectApiList();

  /**
   * 新增智能体发布信息
   *
   * @param appPublish 发布信息
   * @return 结果
   */
  int insertAppPublish(@Param("dto") AppPublishDTO appPublish);

  /**
   * 删除智能体发布
   *
   * @param publishId 主键 ID
   * @param updatorId 操作人 ID
   * @return 结果
   */
  int deleteAppPublish(@Param("publishId") Long publishId, @Param("updatorId") Long updatorId);

  /**
   * 获取智能体发布列表
   *
   * @param tenantId 租户 ID
   * @return 发布列表
   */
  List<AppPublishDTO> selectAppPublishList(@Param("tenantId") Long tenantId, @Param("botName") String botName, @Param("botId") Long botId);
}
