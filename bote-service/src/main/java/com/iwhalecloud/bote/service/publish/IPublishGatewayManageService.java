package com.iwhalecloud.bote.service.publish;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.publish.PublishGatewayDTO;
import com.iwhalecloud.bote.dto.publish.PublishGatewayQueryParams;
import com.iwhalecloud.bote.dto.publish.PublishGatewaySimpleDTO;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;

/**
 * 在线环境维护服务
 *
 * @author lizuyin
 * @since 2026-01-21
 */
public interface IPublishGatewayManageService {

  /**
   * 查询单个网关
   *
   * @param tenantId 租户 ID
   * @param gatewayId 网关主键
   * @return 网关
   */
  PublishGatewayDTO findPublishGateway(Long tenantId, Long gatewayId);

  /**
   * 查询单个网关（简单信息，仅包含必要字段）
   *
   * @param tenantId 租户 ID
   * @param gatewayId 网关主键
   * @return 网关简单信息
   */
  PublishGatewaySimpleDTO findPublishGatewaySimple(Long tenantId, Long gatewayId);

  /**
   * 保存网关（新增或更新）
   *
   * @param dto 网关
   * @return 结果
   */
  ResultVO<PublishGatewayDTO> savePublishGateway(PublishGatewayDTO dto);

  /**
   * 删除网关
   *
   * @param tenantId 租户 ID
   * @param gatewayId 网关主键
   * @return 结果
   */
  ResultVO<Void> deletePublishGateway(Long tenantId, Long gatewayId);

  /**
   * 查询网关列表（分页）
   *
   * @param queryParams 查询条件
   * @return 网关分页列表
   */
  PageInfo<PublishGatewayDTO> queryPublishGatewayPage(PublishGatewayQueryParams queryParams);
}

