package com.iwhalecloud.bote.mapper.publish;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.publish.PublishGatewayDTO;
import com.iwhalecloud.bote.dto.publish.PublishGatewayQueryParams;
import com.iwhalecloud.bote.dto.publish.PublishGatewaySimpleDTO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * 在线环境维护 MAPPER
 *
 * @author lizuyin
 * @since 2026-01-21
 */
public interface PublishGatewayManageMapper {

  /**
   * 根据主键获取网关
   *
   * @param tenantId 租户 ID
   * @param gatewayId 网关主键
   * @return 网关
   */
  PublishGatewayDTO getPublishGateway(@Param("tenantId") Long tenantId, @Param("id") Long gatewayId);

  /**
   * 根据主键获取网关（简单信息，仅包含必要字段）
   *
   * @param tenantId 租户 ID
   * @param gatewayId 网关主键
   * @return 网关简单信息
   */
  PublishGatewaySimpleDTO getPublishGatewaySimple(@Param("tenantId") Long tenantId, @Param("id") Long gatewayId);

  /**
   * 新增网关
   *
   * @param dto 网关
   * @return 结果
   */
  int insertPublishGateway(@Param("dto") PublishGatewayDTO dto);

  /**
   * 修改网关
   *
   * @param dto 网关
   * @return 结果
   */
  int updatePublishGateway(@Param("dto") PublishGatewayDTO dto);

  /**
   * 删除网关（逻辑删除）
   *
   * @param tenantId 租户 ID
   * @param gatewayId 网关主键
   * @param updatorId 操作人 ID
   * @return 结果
   */
  int deletePublishGateway(@Param("tenantId") Long tenantId, @Param("gatewayId") Long gatewayId, @Param("updatorId") Long updatorId);

  /**
   * 获取网关列表（分页）
   *
   * @param queryParams 查询条件
   * @param rowBounds 分页参数
   * @return 网关分页列表
   */
  Page<PublishGatewayDTO> selectPublishGatewayPage(@Param("query") PublishGatewayQueryParams queryParams, RowBounds rowBounds);
}

